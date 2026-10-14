package com.aniverse.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.aniverse.app.ui.components.AniverseBottomBar
import com.aniverse.app.ui.components.NowPlayingBar
import com.aniverse.app.ui.screens.home.HomeScreen
import com.aniverse.app.ui.screens.player.PlayerScreen
import com.aniverse.app.ui.screens.profile.ProfileScreen
import com.aniverse.app.ui.screens.search.SearchScreen
import com.aniverse.app.ui.screens.watchlist.WatchlistScreen
import com.aniverse.app.ui.theme.DarkCanvas
import com.aniverse.app.util.NowPlayingController

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = DarkCanvas,
        bottomBar = {
            if (currentRoute?.startsWith("player") != true) {
                Column {
                    NowPlayingBar(
                        onBarClick = {
                            if (NowPlayingController.animeId > 0) {
                                navController.navigate(
                                    Screen.Player.createRoute(
                                        animeId = NowPlayingController.animeId,
                                        title = NowPlayingController.animeTitle,
                                        englishTitle = NowPlayingController.englishTitle,
                                        cover = NowPlayingController.coverUrl,
                                        format = NowPlayingController.format
                                    )
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                    AniverseBottomBar(navController = navController)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAnimeClick = { anime ->
                        navController.navigate(
                            Screen.Player.createRoute(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                cover = anime.coverImage,
                                format = anime.format
                            )
                        )
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onAnimeClick = { anime ->
                        navController.navigate(
                            Screen.Player.createRoute(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                cover = anime.coverImage,
                                format = anime.format
                            )
                        )
                    }
                )
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    onAnimeClick = { anime ->
                        navController.navigate(
                            Screen.Player.createRoute(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                cover = anime.coverImage,
                                format = anime.format
                            )
                        )
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("animeId") { type = NavType.IntType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("englishTitle") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("cover") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("format") {
                        type = NavType.StringType
                        defaultValue = "TV"
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
                val title = try { java.net.URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
                val rawEng = backStackEntry.arguments?.getString("englishTitle")?.takeIf { it.isNotEmpty() }
                val englishTitle = rawEng?.let { try { java.net.URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it } }
                val cover = backStackEntry.arguments?.getString("cover") ?: ""
                val format = backStackEntry.arguments?.getString("format") ?: "TV"

                PlayerScreen(
                    animeId = animeId,
                    animeTitle = title,
                    englishTitle = englishTitle,
                    coverUrl = cover,
                    format = format,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
