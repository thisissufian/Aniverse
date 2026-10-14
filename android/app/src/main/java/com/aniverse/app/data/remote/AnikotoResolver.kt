package com.aniverse.app.data.remote

import android.util.Log
import com.aniverse.app.data.model.Episode
import com.aniverse.app.data.model.ServerItem
import com.aniverse.app.data.model.SkipInterval
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class StreamEmbedResult(
    val embedUrl: String,
    val intro: SkipInterval? = null,
    val outro: SkipInterval? = null
)

class AnikotoResolver(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    private val tag = "AnikotoResolver"
    private val baseUrl = "https://anikototv.to"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    private val headers = mapOf(
        "X-Requested-With" to "XMLHttpRequest",
        "Referer" to "$baseUrl/",
        "User-Agent" to userAgent
    )

    private fun newRequest(url: String): Request {
        val builder = Request.Builder().url(url)
        headers.forEach { (k, v) -> builder.header(k, v) }
        return builder.build()
    }

    suspend fun getEpisodes(
        primaryTitle: String,
        secondaryTitle: String? = null,
        format: String? = "TV"
    ): List<Episode> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Resolving episodes for primary: '$primaryTitle', secondary: '$secondaryTitle', format: '$format'")

            val termType = when (format?.uppercase()) {
                "MOVIE" -> "Movie"
                "OVA" -> "OVA"
                "ONA" -> "ONA"
                "SPECIAL" -> "Special"
                else -> "TV"
            }

            // Generate search queries to try in order of precision
            val searchQueries = mutableListOf<String>()

            // 1. Try English title first (Anikoto search prefers English/International titles)
            secondaryTitle?.trim()?.takeIf { it.isNotEmpty() }?.let { eng ->
                searchQueries.add(eng)
            }

            // 2. Try primary (Romaji) title
            primaryTitle.trim().takeIf { it.isNotEmpty() && !searchQueries.contains(it) }?.let { rom ->
                searchQueries.add(rom)
            }

            // 3. Try stripped titles (remove colons, dashes, season subtitles, brackets)
            val cleanRegex = Regex("[:\\-–—_()\\[\\]]")
            secondaryTitle?.let { eng ->
                val cleaned = eng.replace(cleanRegex, " ").replace(Regex("\\s+"), " ").trim()
                if (cleaned.isNotEmpty() && !searchQueries.contains(cleaned)) {
                    searchQueries.add(cleaned)
                }
            }
            val cleanedPrimary = primaryTitle.replace(cleanRegex, " ").replace(Regex("\\s+"), " ").trim()
            if (cleanedPrimary.isNotEmpty() && !searchQueries.contains(cleanedPrimary)) {
                searchQueries.add(cleanedPrimary)
            }

            var slug: String? = null

            for (query in searchQueries) {
                // First attempt: search with format filter (e.g. TV / Movie)
                slug = findSlugForQuery(query, termType)
                if (slug != null) {
                    Log.d(tag, "Found slug '$slug' for query '$query' (with format $termType)")
                    break
                }

                // Second attempt: search without format filter
                slug = findSlugForQuery(query, null)
                if (slug != null) {
                    Log.d(tag, "Found slug '$slug' for query '$query' (unfiltered)")
                    break
                }
            }

            if (slug == null) {
                Log.w(tag, "No slug found for any query in $searchQueries")
                return@withContext emptyList()
            }

            // Step 2: Get anime internal data-id from watch page
            var dataId = fetchAnimeDataId(slug)
            if (dataId == null) {
                Log.w(tag, "Could not extract data-id from /watch/$slug")
                return@withContext emptyList()
            }

            // Step 3: Fetch episode list via AJAX endpoint
            val epListUrl = "$baseUrl/ajax/episode/list/$dataId"
            val epListJsonStr = client.newCall(newRequest(epListUrl)).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(tag, "Failed to fetch episode list: HTTP ${response.code}")
                    return@withContext emptyList()
                }
                response.body?.string() ?: ""
            }

            val json = JsonParser.parseString(epListJsonStr).asJsonObject
            if (json.get("status")?.asInt != 200) {
                Log.e(tag, "Anikoto episode list status not 200: $epListJsonStr")
                return@withContext emptyList()
            }

            val resultHtml = json.get("result")?.asString ?: return@withContext emptyList()
            val epDoc = Jsoup.parse(resultHtml)

            val episodes = mutableListOf<Episode>()
            val epLinks = epDoc.select("a[data-id]")
            for (element in epLinks) {
                val id = element.attr("data-id")
                val num = element.attr("data-num").toIntOrNull() ?: 1
                val title = element.attr("title").ifEmpty { "Episode $num" }
                val hasSub = element.attr("data-sub") == "1"
                val hasDub = element.attr("data-dub") == "1"
                val dataIds = element.attr("data-ids")

                episodes.add(
                    Episode(
                        id = id,
                        number = num,
                        title = title,
                        hasSub = hasSub,
                        hasDub = hasDub,
                        dataIds = dataIds
                    )
                )
            }

            Log.d(tag, "Successfully fetched ${episodes.size} episodes for slug '$slug'")
            episodes
        } catch (e: Exception) {
            Log.e(tag, "Error in getEpisodes", e)
            emptyList()
        }
    }

    private fun findSlugForQuery(query: String, termType: String?): String? {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = if (!termType.isNullOrEmpty()) {
                val encodedTerm = URLEncoder.encode(termType, "UTF-8")
                "$baseUrl/filter?keyword=$encoded&term_type[]=$encodedTerm"
            } else {
                "$baseUrl/filter?keyword=$encoded"
            }

            val html = client.newCall(newRequest(url)).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string() ?: ""
            }

            val doc = Jsoup.parse(html)
            val watchLinks = doc.select("a[href*=/watch/]")
            for (element in watchLinks) {
                val href = element.attr("href")
                if (href.isNotEmpty()) {
                    val match = Regex("/(?:watch|anime)/([^/]+)").find(href)
                    if (match != null) {
                        return match.groupValues[1]
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.w(tag, "findSlugForQuery failed for query '$query': ${e.message}")
            null
        }
    }

    private fun fetchAnimeDataId(slug: String): String? {
        return try {
            val watchUrl = "$baseUrl/watch/$slug/ep-1"
            var html = client.newCall(newRequest(watchUrl)).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }

            if (html == null) {
                // Fallback to base watch page
                val altUrl = "$baseUrl/watch/$slug"
                html = client.newCall(newRequest(altUrl)).execute().use { response ->
                    if (response.isSuccessful) response.body?.string() else null
                }
            }

            if (html == null) return null

            val doc = Jsoup.parse(html)
            val dataId = doc.select("[data-id][data-fetch]").attr("data-id")
            if (dataId.isNotEmpty()) dataId else null
        } catch (e: Exception) {
            Log.w(tag, "fetchAnimeDataId failed for slug '$slug': ${e.message}")
            null
        }
    }

    suspend fun getAvailableServers(dataIds: String): List<ServerItem> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/ajax/server/list?servers=" + URLEncoder.encode(dataIds, "UTF-8")
            val jsonStr = client.newCall(newRequest(url)).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: ""
            }

            val json = JsonParser.parseString(jsonStr).asJsonObject
            if (json.get("status")?.asInt != 200) return@withContext emptyList()

            val html = json.get("result")?.asString ?: return@withContext emptyList()
            val doc = Jsoup.parse(html)

            val servers = mutableListOf<ServerItem>()
            val items = doc.select("li[data-link-id]")
            for (el in items) {
                val linkId = el.attr("data-link-id")
                val serverId = el.attr("data-sv-id")
                val type = el.parents().select("div[data-type]").attr("data-type").ifEmpty { "sub" }
                val name = el.text().trim()

                servers.add(
                    ServerItem(
                        linkId = linkId,
                        serverId = serverId,
                        name = name,
                        type = type
                    )
                )
            }
            servers
        } catch (e: Exception) {
            Log.e(tag, "Error in getAvailableServers", e)
            emptyList()
        }
    }

    suspend fun getServerEmbed(linkId: String): StreamEmbedResult? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/ajax/server?get=" + URLEncoder.encode(linkId, "UTF-8")
            val jsonStr = client.newCall(newRequest(url)).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body?.string() ?: ""
            }

            val json = JsonParser.parseString(jsonStr).asJsonObject
            if (json.get("status")?.asInt != 200) return@withContext null

            val resultObj = json.getAsJsonObject("result") ?: return@withContext null
            val embedUrl = resultObj.get("url")?.asString ?: return@withContext null

            var introInterval: SkipInterval? = null
            var outroInterval: SkipInterval? = null

            val skipData = resultObj.getAsJsonObject("skip_data")
            if (skipData != null) {
                val introArr = skipData.getAsJsonArray("intro")
                if (introArr != null && introArr.size() >= 2) {
                    val startSec = introArr.get(0).asLong
                    val endSec = introArr.get(1).asLong
                    introInterval = SkipInterval(startSec * 1000, endSec * 1000)
                }

                val outroArr = skipData.getAsJsonArray("outro")
                if (outroArr != null && outroArr.size() >= 2) {
                    val startSec = outroArr.get(0).asLong
                    val endSec = outroArr.get(1).asLong
                    outroInterval = SkipInterval(startSec * 1000, endSec * 1000)
                }
            }

            StreamEmbedResult(
                embedUrl = embedUrl,
                intro = introInterval,
                outro = outroInterval
            )
        } catch (e: Exception) {
            Log.e(tag, "Error in getServerEmbed", e)
            null
        }
    }
}
