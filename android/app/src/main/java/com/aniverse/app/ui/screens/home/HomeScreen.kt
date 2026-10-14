package com.aniverse.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.aniverse.app.data.local.WatchHistoryItem
import com.aniverse.app.data.model.Anime
import com.aniverse.app.ui.components.AnimeSection
import com.aniverse.app.ui.components.HeroBanner
import com.aniverse.app.ui.theme.*

@Composable
fun HomeScreen(
    onAnimeClick: (Anime) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshHistory()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        if (uiState.isLoading && uiState.heroAnime == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (uiState.errorMessage != null && uiState.top10Today.isEmpty() && uiState.mostPopular.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Network error",
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Featured Hero Banner
                item {
                    HeroBanner(
                        anime = uiState.heroAnime,
                        onWatchClick = { onAnimeClick(it) }
                    )
                }

                // Continue Watching Shelf
                if (uiState.continueWatching.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Continue Watching",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.continueWatching, key = { it.animeId }) { item ->
                                    ContinueWatchingCard(
                                        item = item,
                                        onClick = {
                                            onAnimeClick(
                                                Anime(
                                                    id = item.animeId,
                                                    title = item.title,
                                                    coverImage = item.coverUrl
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. Top 10 Today
                if (uiState.top10Today.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        AnimeSection(
                            title = "Top 10 Today",
                            subtitle = "Most trending anime in the last 24 hours",
                            animeList = uiState.top10Today,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 2. Top 10 Week
                if (uiState.top10Week.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimeSection(
                            title = "Top 10 Week",
                            subtitle = "Community favorites this week",
                            animeList = uiState.top10Week,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 3. Top 10 Month
                if (uiState.top10Month.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimeSection(
                            title = "Top 10 Month",
                            subtitle = "Highest rated anime this month",
                            animeList = uiState.top10Month,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 4. Top Airing
                if (uiState.topAiring.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimeSection(
                            title = "Top Airing",
                            subtitle = "Currently releasing seasonal anime",
                            animeList = uiState.topAiring,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 5. Most Popular
                if (uiState.mostPopular.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimeSection(
                            title = "Most Popular",
                            subtitle = "All-time legendary favorites",
                            animeList = uiState.mostPopular,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 6. Most Watched
                if (uiState.mostWatched.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimeSection(
                            title = "Most Watched",
                            subtitle = "Highest community watch counts",
                            animeList = uiState.mostWatched,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 7. Chinese Donghua
                if (uiState.donghuaAnime.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimeSection(
                            title = "Chinese Donghua",
                            subtitle = "Top 3D and 2D cultivation anime",
                            animeList = uiState.donghuaAnime,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: WatchHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (item.durationMs > 0) {
        (item.positionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Row(
        modifier = modifier
            .width(220.dp)
            .height(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkCard)
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Episode ${item.episodeNumber}",
                color = PrimaryViolet,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PrimaryViolet,
                trackColor = DarkBorder
            )
        }
    }
}
