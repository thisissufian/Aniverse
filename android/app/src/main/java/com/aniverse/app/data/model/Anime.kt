package com.aniverse.app.data.model

data class Anime(
    val id: Int,
    val title: String,
    val englishTitle: String? = null,
    val coverImage: String = "",
    val bannerImage: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val averageScore: Int? = null,
    val episodes: Int? = null,
    val currentEpisode: Int? = null,
    val status: String? = null,
    val format: String? = null,
    val subCount: Int? = null,
    val dubCount: Int? = null,
    val isDonghua: Boolean = false,
    val seasonYear: Int? = null
)
