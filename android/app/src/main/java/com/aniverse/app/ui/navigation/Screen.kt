package com.aniverse.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)

    object Player : Screen(
        route = "player/{animeId}?title={title}&cover={cover}",
        title = "Player",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ) {
        fun createRoute(animeId: Int, title: String = "", cover: String = ""): String {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val encodedCover = java.net.URLEncoder.encode(cover, "UTF-8")
            return "player/$animeId?title=$encodedTitle&cover=$encodedCover"
        }
    }
}
