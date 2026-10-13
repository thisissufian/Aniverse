package com.aniverse.app.data.remote

import com.aniverse.app.data.model.Episode
import com.aniverse.app.data.model.ServerItem
import com.aniverse.app.data.model.SkipInterval
import com.google.gson.JsonObject
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    private val baseUrl = "https://anikototv.to"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    private val localServerUrl = "http://10.0.2.2:5000" // Android emulator loopback to PC

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

    suspend fun getEpisodes(animeTitle: String, format: String? = "TV"): List<Episode> = withContext(Dispatchers.IO) {
        try {
            // First try local server if user has desktop backend running
            val localEpisodes = tryFetchLocalServerEpisodes(animeTitle)
            if (localEpisodes.isNotEmpty()) return@withContext localEpisodes

            // Step 1: Search anime slug on Anikoto
            val termType = when (format?.uppercase()) {
                "MOVIE" -> "Movie"
                "OVA" -> "OVA"
                "ONA" -> "ONA"
                "SPECIAL" -> "Special"
                else -> "TV"
            }

            val encodedTitle = URLEncoder.encode(animeTitle, "UTF-8")
            val searchUrl = "$baseUrl/filter?keyword=$encodedTitle&term_type[]=$termType"

            val searchHtml = client.newCall(newRequest(searchUrl)).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: ""
            }

            val doc = Jsoup.parse(searchHtml)
            var slug: String? = null

            val watchLinks = doc.select("a[href*=/watch/]")
            for (element in watchLinks) {
                val href = element.attr("href")
                val text = element.text().trim()
                if (href.isNotEmpty() && text.length > 3) {
                    val match = Regex("/watch/([^/]+)").find(href)
                    if (match != null) {
                        slug = match.groupValues[1]
                        break
                    }
                }
            }

            if (slug == null) return@withContext emptyList()

            // Step 2: Get anime internal data-id
            val watchUrl = "$baseUrl/watch/$slug/ep-1"
            val watchHtml = client.newCall(newRequest(watchUrl)).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: ""
            }

            val watchDoc = Jsoup.parse(watchHtml)
            val dataId = watchDoc.select("[data-id][data-fetch]").attr("data-id")
            if (dataId.isEmpty()) return@withContext emptyList()

            // Step 3: Get episode list
            val epListUrl = "$baseUrl/ajax/episode/list/$dataId"
            val epListJsonStr = client.newCall(newRequest(epListUrl)).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: ""
            }

            val json = JsonParser.parseString(epListJsonStr).asJsonObject
            if (json.get("status")?.asInt != 200) return@withContext emptyList()

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
            episodes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
            e.printStackTrace()
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
            e.printStackTrace()
            null
        }
    }

    private fun tryFetchLocalServerEpisodes(title: String): List<Episode> {
        return try {
            val request = Request.Builder()
                .url("$localServerUrl/api/stream/search?q=" + URLEncoder.encode(title, "UTF-8"))
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val json = JsonParser.parseString(body).asJsonObject
                val array = json.getAsJsonArray("episodes") ?: return emptyList()
                val list = mutableListOf<Episode>()
                array.forEach {
                    val item = it.asJsonObject
                    list.add(
                        Episode(
                            id = item.get("id").asString,
                            number = item.get("number").asInt,
                            title = item.get("title").asString,
                            dataIds = item.get("dataIds")?.asString
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
