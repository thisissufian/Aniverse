package com.aniverse.app.data.local

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aniverse_prefs", Context.MODE_PRIVATE)

    var autoSkipIntro: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SKIP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SKIP, value).apply()

    var autoPlayNext: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PLAY_NEXT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_PLAY_NEXT, value).apply()

    var preferDub: Boolean
        get() = prefs.getBoolean(KEY_PREFER_DUB, false)
        set(value) = prefs.edit().putBoolean(KEY_PREFER_DUB, value).apply()

    companion object {
        private const val KEY_AUTO_SKIP = "auto_skip_intro"
        private const val KEY_AUTO_PLAY_NEXT = "auto_play_next"
        private const val KEY_PREFER_DUB = "prefer_dub"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
