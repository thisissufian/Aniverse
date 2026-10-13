package com.aniverse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.aniverse.app.ui.navigation.NavGraph
import com.aniverse.app.ui.theme.AniverseTheme

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
}
