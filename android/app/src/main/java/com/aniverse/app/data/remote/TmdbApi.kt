package com.aniverse.app.data.remote

import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.model.Episode
import com.aniverse.app.data.model.ServerItem
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TmdbApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    private val apiKey = "4e44d9029b1270a757cddc766a1bcb63"
    private val baseUrl = "https://api.themoviedb.org/3"
    private val imageBaseUrl = "https://image.tmdb.org/t/p/w500"
    private val backdropBaseUrl = "https://image.tmdb.org/t/p/original"

    private suspend fun getJson(url: String): JsonObject? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
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

    private fun parseMediaItem(obj: JsonObject, defaultType: String? = null, mode: String? = null): Anime {
        val id = obj.get("id")?.asInt ?: 0
        val type = defaultType ?: obj.get("media_type")?.asString ?: if (obj.has("first_air_date")) "tv" else "movie"
        val title = obj.get("title")?.asString 
            ?: obj.get("name")?.asString 
            ?: obj.get("original_title")?.asString 
            ?: obj.get("original_name")?.asString 
            ?: "Untitled"
        val originalTitle = obj.get("original_title")?.asString ?: obj.get("original_name")?.asString

        val posterPath = obj.get("poster_path")?.let { if (!it.isJsonNull) it.asString else null }
        val backdropPath = obj.get("backdrop_path")?.let { if (!it.isJsonNull) it.asString else null }
        val voteAverage = obj.get("vote_average")?.asDouble ?: 0.0
        val releaseDate = obj.get("release_date")?.asString ?: obj.get("first_air_date")?.asString
        val year = releaseDate?.split("-")?.firstOrNull()?.toIntOrNull()

        return Anime(
            id = id,
            title = title,
            englishTitle = originalTitle,
            coverImage = if (posterPath != null) "$imageBaseUrl$posterPath" else "",
            bannerImage = if (backdropPath != null) "$backdropBaseUrl$backdropPath" else null,
            description = obj.get("overview")?.asString,
            averageScore = if (voteAverage > 0) (voteAverage * 10).toInt() else null,
            format = if (type == "tv") "TV" else "MOVIE",
            seasonYear = year,
            mediaType = type,
            mode = mode
        )
    }

    suspend fun getKDramaHome(): Map<String, List<Anime>> = withContext(Dispatchers.IO) {
        val trendingUrl = "$baseUrl/discover/tv?api_key=$apiKey&with_original_language=ko&sort_by=popularity.desc&vote_count.gte=5"
        val moviesUrl = "$baseUrl/discover/movie?api_key=$apiKey&with_original_language=ko&sort_by=popularity.desc&vote_count.gte=5"
        val topRatedUrl = "$baseUrl/discover/tv?api_key=$apiKey&with_original_language=ko&sort_by=vote_average.desc&vote_count.gte=80"

        val trendingJson = getJson(trendingUrl)
        val moviesJson = getJson(moviesUrl)
        val topRatedJson = getJson(topRatedUrl)

        val trendingList = (trendingJson?.getAsJsonArray("results") ?: JsonArray()).map {
            parseMediaItem(it.asJsonObject, "tv", "kdrama")
        }
        val moviesList = (moviesJson?.getAsJsonArray("results") ?: JsonArray()).map {
            parseMediaItem(it.asJsonObject, "movie", "kdrama")
        }
        val topRatedList = (topRatedJson?.getAsJsonArray("results") ?: JsonArray()).map {
            parseMediaItem(it.asJsonObject, "tv", "kdrama")
        }

        mapOf(
            "trending" to trendingList,
            "movies" to moviesList,
            "topRated" to topRatedList
        )
    }

    suspend fun getHollywoodHome(): Map<String, List<Anime>> = withContext(Dispatchers.IO) {
        val trendingUrl = "$baseUrl/trending/all/week?api_key=$apiKey"
        val moviesUrl = "$baseUrl/movie/popular?api_key=$apiKey"
        val tvUrl = "$baseUrl/tv/popular?api_key=$apiKey"

        val trendingJson = getJson(trendingUrl)
        val moviesJson = getJson(moviesUrl)
        val tvJson = getJson(tvUrl)

        val trendingList = (trendingJson?.getAsJsonArray("results") ?: JsonArray()).map {
            parseMediaItem(it.asJsonObject, null, "hollywood")
        }
        val moviesList = (moviesJson?.getAsJsonArray("results") ?: JsonArray()).map {
            parseMediaItem(it.asJsonObject, "movie", "hollywood")
        }
        val tvList = (tvJson?.getAsJsonArray("results") ?: JsonArray()).map {
            parseMediaItem(it.asJsonObject, "tv", "hollywood")
        }

        mapOf(
            "trending" to trendingList,
            "movies" to moviesList,
            "tv" to tvList
        )
    }

    suspend fun searchMedia(mode: String, query: String, type: String = "all", page: Int = 1): List<Anime> = withContext(Dispatchers.IO) {
        val endpoint = if (query.isNotBlank()) {
            when (type) {
                "movie" -> "$baseUrl/search/movie?api_key=$apiKey&query=$query&page=$page&include_adult=false"
                "tv" -> "$baseUrl/search/tv?api_key=$apiKey&query=$query&page=$page&include_adult=false"
                else -> "$baseUrl/search/multi?api_key=$apiKey&query=$query&page=$page&include_adult=false"
            }
        } else {
            val discType = if (type == "movie") "movie" else "tv"
            var url = "$baseUrl/discover/$discType?api_key=$apiKey&sort_by=popularity.desc&page=$page"
            if (mode == "kdrama") url += "&with_original_language=ko"
            url
        }

        val json = getJson(endpoint) ?: return@withContext emptyList()
        val results = json.getAsJsonArray("results") ?: return@withContext emptyList()

        results.mapNotNull { element ->
            val obj = element.asJsonObject
            val mediaType = obj.get("media_type")?.asString
            if (mediaType == "person") return@mapNotNull null
            if (mode == "kdrama" && query.isNotBlank()) {
                val origLang = obj.get("original_language")?.asString
                if (origLang != "ko") return@mapNotNull null
            }
            parseMediaItem(obj, if (type != "all") type else null, mode)
        }
    }

    suspend fun getTvSeasonEpisodes(tvId: Int, season: Int = 1): List<Episode> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/tv/$tvId/season/$season?api_key=$apiKey"
        val json = getJson(url) ?: return@withContext emptyList()
        val episodes = json.getAsJsonArray("episodes") ?: return@withContext emptyList()

        episodes.map { element ->
            val obj = element.asJsonObject
            val epNum = obj.get("episode_number")?.asInt ?: 1
            val epTitle = obj.get("name")?.asString ?: "Episode $epNum"

            Episode(
                id = "${tvId}_s${season}_e$epNum",
                number = epNum,
                title = epTitle,
                hasSub = true,
                hasDub = false,
                dataIds = null
            )
        }
    }

    fun getMediaStreamServers(type: String, id: Int, season: Int = 1, episode: Int = 1): List<ServerItem> {
        val isMovie = type == "movie"
        return listOf(
            ServerItem(
                name = "HD-1 (VidLink)",
                linkId = if (isMovie) "https://vidlink.pro/movie/$id" else "https://vidlink.pro/tv/$id/$season/$episode",
                type = "sub",
                serverId = "vidlink"
            ),
            ServerItem(
                name = "HD-2 (VidSrc)",
                linkId = if (isMovie) "https://vidsrc.to/embed/movie/$id" else "https://vidsrc.to/embed/tv/$id/$season/$episode",
                type = "sub",
                serverId = "vidsrc"
            ),
            ServerItem(
                name = "HD-3 (SuperEmbed)",
                linkId = if (isMovie) "https://multiembed.mov/?video_id=$id&tmdb=1" else "https://multiembed.mov/?video_id=$id&tmdb=1&s=$season&e=$episode",
                type = "sub",
                serverId = "superembed"
            )
        )
    }

    companion object {
        @Volatile
        private var instance: TmdbApi? = null

        fun getInstance(): TmdbApi {
            return instance ?: synchronized(this) {
                instance ?: TmdbApi().also { instance = it }
            }
        }
    }
}
