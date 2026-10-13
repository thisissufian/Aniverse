package com.aniverse.app.data.model

data class Episode(
    val id: String,
    val number: Int,
    val title: String,
    val hasSub: Boolean = true,
    val hasDub: Boolean = false,
    val dataIds: String? = null
)

data class ServerItem(
    val linkId: String,
    val serverId: String,
    val name: String,
    val type: String // "sub" or "dub"
)

data class SkipInterval(
    val start: Long,
    val end: Long
)

data class StreamSource(
    val embedUrl: String? = null,
    val streamUrl: String? = null,
    val intro: SkipInterval? = null,
    val outro: SkipInterval? = null,
    val servers: List<ServerItem> = emptyList()
)
