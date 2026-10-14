package com.aniverse.app.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global controller for Spotify-like "Now Playing" mini-player bar
 * across the entire Android app.
 */
object NowPlayingController {
    var isVisible by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)

    var animeId by mutableStateOf(0)
    var animeTitle by mutableStateOf("")
    var englishTitle by mutableStateOf<String?>(null)
    var coverUrl by mutableStateOf("")
    var format by mutableStateOf("TV")
    var episodeNumber by mutableStateOf(1)
    var episodeTitle by mutableStateOf<String?>(null)
    var positionMs by mutableLongStateOf(0L)
    var durationMs by mutableLongStateOf(1440000L) // Default 24 min

    fun setNowPlaying(
        animeId: Int,
        title: String,
        englishTitle: String? = null,
        coverUrl: String = "",
        format: String? = "TV",
        episodeNumber: Int = 1,
        episodeTitle: String? = null,
        positionMs: Long = 0L,
        durationMs: Long = 1440000L,
        isPlaying: Boolean = true
    ) {
        this.animeId = animeId
        this.animeTitle = title
        this.englishTitle = englishTitle
        this.coverUrl = coverUrl
        this.format = format ?: "TV"
        this.episodeNumber = episodeNumber
        this.episodeTitle = episodeTitle
        this.positionMs = positionMs
        this.durationMs = if (durationMs > 0) durationMs else 1440000L
        this.isPlaying = isPlaying
        this.isVisible = true
    }

    fun updateProgress(pos: Long, dur: Long) {
        this.positionMs = pos
        if (dur > 0) {
            this.durationMs = dur
        }
    }

    fun setPlaybackState(playing: Boolean) {
        this.isPlaying = playing
    }

    fun togglePlayPause() {
        this.isPlaying = !this.isPlaying
        PipController.isVideoPlaying = this.isPlaying
    }

    fun dismiss() {
        this.isVisible = false
        this.isPlaying = false
        PipController.isVideoPlaying = false
    }

    fun formatTimestamp(ms: Long): String {
        val totalSecs = (ms / 1000).coerceAtLeast(0)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return "%02d:%02d".format(mins, secs)
    }
}
