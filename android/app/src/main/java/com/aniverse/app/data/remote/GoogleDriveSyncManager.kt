package com.aniverse.app.data.remote

import android.content.Context
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.SyncPayload
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GoogleDriveSyncManager(private val context: Context) {

    private val dbHelper = AniverseDbHelper.getInstance(context)
    private val client = OkHttpClient()
    private val gson = Gson()

    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val SYNC_FILE_NAME = "aniverse_sync.json"

        @Volatile
        private var instance: GoogleDriveSyncManager? = null

        fun getInstance(context: Context): GoogleDriveSyncManager {
            return instance ?: synchronized(this) {
                instance ?: GoogleDriveSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DRIVE_APPDATA_SCOPE))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, Scope(DRIVE_APPDATA_SCOPE))) {
            account
        } else null
    }

    private suspend fun getAccessToken(account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
        try {
            val androidAccount = account.account
                ?: android.accounts.Account(account.email ?: "", "com.google")
            GoogleAuthUtil.getToken(context, androidAccount, "oauth2:$DRIVE_APPDATA_SCOPE")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun findSyncFileId(token: String): String? = withContext(Dispatchers.IO) {
        val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='$SYNC_FILE_NAME' and trashed=false&fields=files(id,name,modifiedTime)"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val json = gson.fromJson(body, JsonObject::class.java)
            val files = json.getAsJsonArray("files")
            if (files != null && files.size() > 0) {
                files[0].asJsonObject.get("id").asString
            } else null
        }
    }

    suspend fun syncNow(): Result<SyncPayload> = withContext(Dispatchers.IO) {
        val account = getSignedInAccount()
            ?: return@withContext Result.failure(IllegalStateException("Not signed in to Google"))

        val token = getAccessToken(account)
            ?: return@withContext Result.failure(IllegalStateException("Failed to obtain Google Drive token"))

        try {
            // 1. Download Remote Data
            val fileId = findSyncFileId(token)
            if (fileId != null) {
                val downloadReq = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(downloadReq).execute().use { res ->
                    if (res.isSuccessful) {
                        val body = res.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val remotePayload = gson.fromJson(body, SyncPayload::class.java)
                            dbHelper.importSyncPayload(remotePayload)
                        }
                    }
                }
            }

            // 2. Export Merged Local Data & Upload to Google Drive AppData
            val mergedPayload = dbHelper.exportSyncPayload()
            val jsonContent = gson.toJson(mergedPayload)

            val boundary = "-------314159265358979323846"
            val metadataJson = if (fileId == null) {
                """{"name":"$SYNC_FILE_NAME","mimeType":"application/json","parents":["appDataFolder"]}"""
            } else {
                """{"name":"$SYNC_FILE_NAME","mimeType":"application/json"}"""
            }

            val multipartBody = StringBuilder()
                .append("--$boundary\r\n")
                .append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                .append(metadataJson)
                .append("\r\n--$boundary\r\n")
                .append("Content-Type: application/json\r\n\r\n")
                .append(jsonContent)
                .append("\r\n--$boundary--")
                .toString()

            val uploadUrl = if (fileId != null) {
                "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=multipart"
            } else {
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
            }

            val requestBody = multipartBody.toRequestBody("multipart/related; boundary=$boundary".toMediaType())
            val uploadReq = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $token")
                .method(if (fileId != null) "PATCH" else "POST", requestBody)
                .build()

            client.newCall(uploadReq).execute().use { uploadRes ->
                if (!uploadRes.isSuccessful) {
                    return@withContext Result.failure(IOException("Upload failed with HTTP ${uploadRes.code}"))
                }
            }

            Result.success(mergedPayload)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
