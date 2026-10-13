package com.aniverse.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aniverse.app.ui.components.AniverseBottomBar
import com.aniverse.app.ui.screens.home.HomeScreen
import com.aniverse.app.ui.screens.player.PlayerScreen
import com.aniverse.app.ui.screens.profile.ProfileScreen
import com.aniverse.app.ui.screens.search.SearchScreen
import com.aniverse.app.ui.screens.watchlist.WatchlistScreen
import com.aniverse.app.ui.theme.DarkCanvas

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = DarkCanvas,
        bottomBar = {
            AniverseBottomBar(navController = navController)
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
                                title = anime.englishTitle ?: anime.title,
                                cover = anime.coverImage
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
                                title = anime.englishTitle ?: anime.title,
                                cover = anime.coverImage
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
                                title = anime.englishTitle ?: anime.title,
                                cover = anime.coverImage
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
                        defaultValue = "Episode 1"
                    },
                    navArgument("cover") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val cover = backStackEntry.arguments?.getString("cover") ?: ""

                PlayerScreen(
                    animeId = animeId,
                    animeTitle = title,
                    coverUrl = cover,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
