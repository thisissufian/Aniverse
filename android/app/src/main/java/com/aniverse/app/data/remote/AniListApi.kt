package com.aniverse.app.data.remote

import com.aniverse.app.data.model.Anime
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AniListApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    private val endpoint = "https://graphql.anilist.co"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private suspend fun executeQuery(query: String, variables: Map<String, Any> = emptyMap()): JsonObject? = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            addProperty("query", query)
            val varsObj = JsonObject()
            variables.forEach { (k, v) ->
                when (v) {
                    is Number -> varsObj.addProperty(k, v)
                    is Boolean -> varsObj.addProperty(k, v)
                    is String -> varsObj.addProperty(k, v)
                }
            }
            add("variables", varsObj)
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                JsonParser.parseString(body).asJsonObject
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getTrendingAnime(page: Int = 1, perPage: Int = 20): List<Anime> {
        val query = """
            query (${'$'}page: Int, ${'$'}perPage: Int) {
              Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                media(type: ANIME, sort: TRENDING_DESC, isAdult: false) {
                  id
                  title { romaji english native }
                  coverImage { extraLarge large }
                  bannerImage
                  description(asHtml: false)
                  genres
                  averageScore
                  episodes
                  status
                  format
                  countryOfOrigin
                }
              }
            }
        """.trimIndent()

        val json = executeQuery(query, mapOf("page" to page, "perPage" to perPage)) ?: return emptyList()
        return parseMediaList(json)
    }

    suspend fun getPopularAnime(page: Int = 1, perPage: Int = 20): List<Anime> {
        val query = """
            query (${'$'}page: Int, ${'$'}perPage: Int) {
              Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                media(type: ANIME, sort: POPULARITY_DESC, isAdult: false) {
                  id
                  title { romaji english native }
                  coverImage { extraLarge large }
                  bannerImage
                  description(asHtml: false)
                  genres
                  averageScore
                  episodes
                  status
                  format
                  countryOfOrigin
                }
              }
            }
        """.trimIndent()

        val json = executeQuery(query, mapOf("page" to page, "perPage" to perPage)) ?: return emptyList()
        return parseMediaList(json)
    }

    suspend fun searchAnime(keyword: String, isDonghua: Boolean = false): List<Anime> {
        val country = if (isDonghua) "CN" else null
        val query = """
            query (${'$'}search: String, ${'$'}country: CountryCode) {
              Page(page: 1, perPage: 30) {
                media(search: ${'$'}search, countryOfOrigin: ${'$'}country, type: ANIME, isAdult: false, sort: SEARCH_MATCH) {
                  id
                  title { romaji english native }
                  coverImage { extraLarge large }
                  bannerImage
                  description(asHtml: false)
                  genres
                  averageScore
                  episodes
                  status
                  format
                  countryOfOrigin
                }
              }
            }
        """.trimIndent()

        val vars = mutableMapOf<String, Any>("search" to keyword)
        if (country != null) vars["country"] = country

        val json = executeQuery(query, vars) ?: return emptyList()
        return parseMediaList(json)
    }

    private fun parseMediaList(json: JsonObject): List<Anime> {
        val list = mutableListOf<Anime>()
        try {
            val mediaArray = json.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("media") ?: return emptyList()

            for (elem in mediaArray) {
                val obj = elem.asJsonObject
                val id = obj.get("id").asInt
                val titleObj = obj.getAsJsonObject("title")
                val romaji = titleObj?.get("romaji")?.takeIf { !it.isJsonNull }?.asString ?: "Anime"
                val english = titleObj?.get("english")?.takeIf { !it.isJsonNull }?.asString
                val coverObj = obj.getAsJsonObject("coverImage")
                val cover = coverObj?.get("extraLarge")?.takeIf { !it.isJsonNull }?.asString
                    ?: coverObj?.get("large")?.takeIf { !it.isJsonNull }?.asString ?: ""
                val banner = obj.get("bannerImage")?.takeIf { !it.isJsonNull }?.asString
                val desc = obj.get("description")?.takeIf { !it.isJsonNull }?.asString
                val score = obj.get("averageScore")?.takeIf { !it.isJsonNull }?.asInt
                val eps = obj.get("episodes")?.takeIf { !it.isJsonNull }?.asInt
                val status = obj.get("status")?.takeIf { !it.isJsonNull }?.asString
                val format = obj.get("format")?.takeIf { !it.isJsonNull }?.asString
                val country = obj.get("countryOfOrigin")?.takeIf { !it.isJsonNull }?.asString

                val genres = mutableListOf<String>()
                obj.getAsJsonArray("genres")?.forEach {
                    if (!it.isJsonNull) genres.add(it.asString)
                }

                list.add(
                    Anime(
                        id = id,
                        title = romaji,
                        englishTitle = english,
                        coverImage = cover,
                        bannerImage = banner,
                        description = desc,
                        genres = genres,
                        averageScore = score,
                        episodes = eps,
                        status = status,
                        format = format,
                        isDonghua = country == "CN"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
