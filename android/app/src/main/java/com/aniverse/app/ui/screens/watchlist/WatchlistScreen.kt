package com.aniverse.app.ui.screens.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.WatchlistItem
import com.aniverse.app.data.model.Anime
import com.aniverse.app.ui.components.AnimeCard
import com.aniverse.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WatchlistScreen(
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dbHelper = remember { AniverseDbHelper.getInstance(context) }
    var watchlistItems by remember { mutableStateOf<List<WatchlistItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        watchlistItems = dbHelper.getWatchlist()
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        // Title Bar
        Text(
            text = "My Watchlist (${watchlistItems.size})",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (watchlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = PrimaryViolet,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Watchlist is Empty",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Explore trending anime and save your favorite series here.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(watchlistItems, key = { it.animeId }) { item ->
                    AnimeCard(
                        anime = Anime(
                            id = item.animeId,
                            title = item.title,
                            coverImage = item.coverUrl,
                            averageScore = item.score,
                            format = item.format
                        ),
                        width = 120,
                        onClick = {
                            onAnimeClick(
                                Anime(
                                    id = item.animeId,
                                    title = item.title,
                                    coverImage = item.coverUrl,
                                    averageScore = item.score,
                                    format = item.format
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
