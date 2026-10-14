package com.aniverse.app.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object PipController {
    @Volatile
    var isPlayerActive: Boolean = false

    @Volatile
    var isVideoPlaying: Boolean = false

    var isInPipMode by mutableStateOf(false)

    fun canEnterPip(): Boolean {
        return isPlayerActive && isVideoPlaying
    }
}

