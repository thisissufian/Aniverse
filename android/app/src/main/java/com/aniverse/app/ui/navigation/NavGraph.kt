package com.aniverse.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.aniverse.app.ui.components.AniverseBottomBar
import com.aniverse.app.ui.screens.history.HistoryScreen
import com.aniverse.app.ui.screens.home.HomeScreen
import com.aniverse.app.ui.screens.player.PlayerScreen
import com.aniverse.app.ui.screens.profile.ProfileScreen
import com.aniverse.app.ui.screens.search.SearchScreen
import com.aniverse.app.ui.screens.watchlist.WatchlistScreen
import com.aniverse.app.ui.theme.DarkCanvas
import com.aniverse.app.util.PlayerManager

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DarkCanvas,
            bottomBar = {
                // Bottom bar with 5 items: Home, Search, History, Watchlist, Profile
                if (!PlayerManager.isActive || PlayerManager.isMinimized) {
                    AniverseBottomBar(navController = navController)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onAnimeClick = { anime ->
                            PlayerManager.openAnime(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                coverUrl = anime.coverImage,
                                format = anime.format
                            )
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onAnimeClick = { anime ->
                            PlayerManager.openAnime(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                coverUrl = anime.coverImage,
                                format = anime.format
                            )
                        }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        onAnimeClick = { anime ->
                            PlayerManager.openAnime(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                coverUrl = anime.coverImage,
                                format = anime.format
                            )
                        }
                    )
                }

                composable(Screen.Watchlist.route) {
                    WatchlistScreen(
                        onAnimeClick = { anime ->
                            PlayerManager.openAnime(
                                animeId = anime.id,
                                title = anime.title,
                                englishTitle = anime.englishTitle,
                                coverUrl = anime.coverImage,
                                format = anime.format
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

                    LaunchedEffect(animeId) {
                        PlayerManager.openAnime(
                            animeId = animeId,
                            title = title,
                            englishTitle = englishTitle,
                            coverUrl = cover,
                            format = format
                        )
                        navController.popBackStack()
                    }
                }
            }
        }

        // Overlay: In-App Video Player (Persistent YouTube-Style Floating Mini-Player & Fullscreen)
        if (PlayerManager.isActive) {
            PlayerScreen(
                animeId = PlayerManager.animeId,
                animeTitle = PlayerManager.animeTitle,
                englishTitle = PlayerManager.englishTitle,
                coverUrl = PlayerManager.coverUrl,
                format = PlayerManager.format,
                isMinimized = PlayerManager.isMinimized,
                onMinimize = { PlayerManager.minimize() },
                onExpand = { PlayerManager.expand() },
                onClose = { PlayerManager.close() },
                onBackClick = {
                    if (!PlayerManager.isMinimized) {
                        PlayerManager.minimize()
                    } else {
                        PlayerManager.close()
                    }
                },
                modifier = if (PlayerManager.isMinimized) {
                    Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 12.dp, bottom = 78.dp)
                } else {
                    Modifier.fillMaxSize()
                }
            )
        }
    }
}
