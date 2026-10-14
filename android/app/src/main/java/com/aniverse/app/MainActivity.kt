package com.aniverse.app

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.aniverse.app.ui.navigation.NavGraph
import com.aniverse.app.ui.theme.AniverseTheme
import com.aniverse.app.util.PipController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AniverseTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Only enter Picture-in-Picture when an active video is actually playing on PlayerScreen
        if (PipController.canEnterPip() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
