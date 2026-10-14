package com.aniverse.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.aniverse.app.data.model.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WatchHistoryItem(
    val animeId: Int,
    val title: String,
    val coverUrl: String,
    val episodeNumber: Int,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
)

data class WatchlistItem(
    val animeId: Int,
    val title: String,
    val coverUrl: String,
    val score: Int?,
    val format: String?,
    val addedAt: Long
)

class AniverseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "aniverse.db"
        const val DATABASE_VERSION = 1

        const val TABLE_HISTORY = "watch_history"
        const val COL_ANIME_ID = "anime_id"
        const val COL_TITLE = "title"
        const val COL_COVER_URL = "cover_url"
        const val COL_EPISODE_NUM = "episode_number"
        const val COL_POSITION_MS = "position_ms"
        const val COL_DURATION_MS = "duration_ms"
        const val COL_UPDATED_AT = "updated_at"

        const val TABLE_WATCHLIST = "watchlist"
        const val COL_SCORE = "score"
        const val COL_FORMAT = "format"
        const val COL_ADDED_AT = "added_at"

        @Volatile
        private var instance: AniverseDbHelper? = null

        fun getInstance(context: Context): AniverseDbHelper =
            instance ?: synchronized(this) {
                instance ?: AniverseDbHelper(context.applicationContext).also { instance = it }
            }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_HISTORY (
                $COL_ANIME_ID INTEGER PRIMARY KEY,
                $COL_TITLE TEXT,
                $COL_COVER_URL TEXT,
                $COL_EPISODE_NUM INTEGER,
                $COL_POSITION_MS INTEGER,
                $COL_DURATION_MS INTEGER,
                $COL_UPDATED_AT INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_WATCHLIST (
                $COL_ANIME_ID INTEGER PRIMARY KEY,
                $COL_TITLE TEXT,
                $COL_COVER_URL TEXT,
                $COL_SCORE INTEGER,
                $COL_FORMAT TEXT,
                $COL_ADDED_AT INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WATCHLIST")
        onCreate(db)
    }

    suspend fun saveProgress(
        animeId: Int,
        title: String,
        coverUrl: String,
        episodeNum: Int,
        positionMs: Long,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ANIME_ID, animeId)
            put(COL_TITLE, title)
            put(COL_COVER_URL, coverUrl)
            put(COL_EPISODE_NUM, episodeNum)
            put(COL_POSITION_MS, positionMs)
            put(COL_DURATION_MS, durationMs)
            put(COL_UPDATED_AT, System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun getWatchHistory(): List<WatchHistoryItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WatchHistoryItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "$COL_UPDATED_AT DESC",
            "30"
        )
        cursor.use {
            while (it.moveToNext()) {
                val animeId = it.getInt(it.getColumnIndexOrThrow(COL_ANIME_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COL_TITLE))
                val cover = it.getString(it.getColumnIndexOrThrow(COL_COVER_URL))
                val ep = it.getInt(it.getColumnIndexOrThrow(COL_EPISODE_NUM))
                val pos = it.getLong(it.getColumnIndexOrThrow(COL_POSITION_MS))
                val dur = it.getLong(it.getColumnIndexOrThrow(COL_DURATION_MS))
                val updated = it.getLong(it.getColumnIndexOrThrow(COL_UPDATED_AT))

                list.add(
                    WatchHistoryItem(
                        animeId = animeId,
                        title = title,
                        coverUrl = cover,
                        episodeNumber = ep,
                        positionMs = pos,
                        durationMs = dur,
                        updatedAt = updated
                    )
                )
            }
        }
        list
    }

    suspend fun deleteHistoryItem(animeId: Int): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rows = db.delete(TABLE_HISTORY, "$COL_ANIME_ID = ?", arrayOf(animeId.toString()))
        rows > 0
    }

    suspend fun clearWatchHistory(): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rows = db.delete(TABLE_HISTORY, null, null)
        rows > 0
    }

    suspend fun toggleWatchlist(anime: Anime): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        if (isInWatchlist(anime.id)) {
            db.delete(TABLE_WATCHLIST, "$COL_ANIME_ID = ?", arrayOf(anime.id.toString()))
            false
        } else {
            val values = ContentValues().apply {
                put(COL_ANIME_ID, anime.id)
                put(COL_TITLE, anime.englishTitle ?: anime.title)
                put(COL_COVER_URL, anime.coverImage)
                put(COL_SCORE, anime.averageScore)
                put(COL_FORMAT, anime.format)
                put(COL_ADDED_AT, System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_WATCHLIST, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            true
        }
    }

    suspend fun isInWatchlist(animeId: Int): Boolean = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_WATCHLIST WHERE $COL_ANIME_ID = ?", arrayOf(animeId.toString()))
        val exists = cursor.count > 0
        cursor.close()
        exists
    }

    suspend fun getWatchlist(): List<WatchlistItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WatchlistItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WATCHLIST, null, null, null, null, null, "$COL_ADDED_AT DESC")
        cursor.use {
            while (it.moveToNext()) {
                val animeId = it.getInt(it.getColumnIndexOrThrow(COL_ANIME_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COL_TITLE))
                val cover = it.getString(it.getColumnIndexOrThrow(COL_COVER_URL))
                val score = if (it.isNull(it.getColumnIndexOrThrow(COL_SCORE))) null else it.getInt(it.getColumnIndexOrThrow(COL_SCORE))
                val format = it.getString(it.getColumnIndexOrThrow(COL_FORMAT))
                val added = it.getLong(it.getColumnIndexOrThrow(COL_ADDED_AT))

                list.add(
                    WatchlistItem(
                        animeId = animeId,
                        title = title,
                        coverUrl = cover,
                        score = score,
                        format = format,
                        addedAt = added
                    )
                )
            }
        }
        list
    }

    suspend fun exportSyncPayload(): SyncPayload = withContext(Dispatchers.IO) {
        SyncPayload(
            history = getWatchHistory(),
            watchlist = getWatchlist()
        )
    }

    suspend fun importSyncPayload(payload: SyncPayload): Int = withContext(Dispatchers.IO) {
        val db = writableDatabase
        var importedCount = 0
        db.beginTransaction()
        try {
            // Import History with LWW (Last-Write-Wins based on updatedAt)
            for (item in payload.history) {
                val cursor = db.query(
                    TABLE_HISTORY,
                    arrayOf(COL_UPDATED_AT),
                    "$COL_ANIME_ID = ?",
                    arrayOf(item.animeId.toString()),
                    null, null, null
                )
                var shouldUpdate = true
                cursor.use {
                    if (it.moveToFirst()) {
                        val localUpdated = it.getLong(0)
                        if (item.updatedAt <= localUpdated) {
                            shouldUpdate = false
                        }
                    }
                }
                if (shouldUpdate) {
                    val values = ContentValues().apply {
                        put(COL_ANIME_ID, item.animeId)
                        put(COL_TITLE, item.title)
                        put(COL_COVER_URL, item.coverUrl)
                        put(COL_EPISODE_NUM, item.episodeNumber)
                        put(COL_POSITION_MS, item.positionMs)
                        put(COL_DURATION_MS, item.durationMs)
                        put(COL_UPDATED_AT, item.updatedAt)
                    }
                    db.insertWithOnConflict(TABLE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                    importedCount++
                }
            }

            // Import Watchlist
            for (item in payload.watchlist) {
                val values = ContentValues().apply {
                    put(COL_ANIME_ID, item.animeId)
                    put(COL_TITLE, item.title)
                    put(COL_COVER_URL, item.coverUrl)
                    put(COL_SCORE, item.score)
                    put(COL_FORMAT, item.format)
                    put(COL_ADDED_AT, item.addedAt)
                }
                db.insertWithOnConflict(TABLE_WATCHLIST, null, values, SQLiteDatabase.CONFLICT_IGNORE)
                importedCount++
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        importedCount
    }
}

data class SyncPayload(
    val version: Int = 1,
    val client: String = "android",
    val syncedAt: Long = System.currentTimeMillis(),
    val history: List<WatchHistoryItem>,
    val watchlist: List<WatchlistItem>
)
