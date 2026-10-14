package com.aniverse.app.util

object PipController {
    @Volatile
    var isPlayerActive: Boolean = false

    @Volatile
    var isVideoPlaying: Boolean = false

    fun canEnterPip(): Boolean {
        return isPlayerActive && isVideoPlaying
    }
}
