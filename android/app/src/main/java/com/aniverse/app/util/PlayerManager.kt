package com.aniverse.app.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global manager for the in-app Video Player.
 * Keeps video playback alive across navigation and powers the YouTube-style
 * floating mini-player window in the bottom-right.
 */
object PlayerManager {
    var isActive by mutableStateOf(false)
    var isMinimized by mutableStateOf(false)

    var animeId by mutableIntStateOf(0)
    var animeTitle by mutableStateOf("")
    var englishTitle by mutableStateOf<String?>(null)
    var coverUrl by mutableStateOf("")
    var format by mutableStateOf("TV")

    var currentEpisodeNumber by mutableIntStateOf(1)
    var positionMs by mutableLongStateOf(0L)
    var durationMs by mutableLongStateOf(1440000L)
    var isPlaying by mutableStateOf(false)

    fun openAnime(
        animeId: Int,
        title: String,
        englishTitle: String? = null,
        coverUrl: String = "",
        format: String? = "TV"
    ) {
        if (this.animeId != animeId) {
            this.animeId = animeId
            this.animeTitle = title
            this.englishTitle = englishTitle
            this.coverUrl = coverUrl
            this.format = format ?: "TV"
            this.positionMs = 0L
        }
        this.isActive = true
        this.isMinimized = false
        PipController.isPlayerActive = true
    }

    fun minimize() {
        this.isMinimized = true
    }

    fun expand() {
        this.isMinimized = false
    }

    fun close() {
        this.isActive = false
        this.isMinimized = false
        this.isPlaying = false
        PipController.isPlayerActive = false
        PipController.isVideoPlaying = false
        NowPlayingController.dismiss()
    }
}
