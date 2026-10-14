package com.aniverse.app.ui.screens.watchlist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.WatchHistoryItem
import com.aniverse.app.data.local.WatchlistItem
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.remote.GoogleDriveSyncManager
import com.aniverse.app.ui.components.AnimeCard
import com.aniverse.app.ui.theme.*
import com.aniverse.app.util.NowPlayingController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WatchlistScreen(
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { AniverseDbHelper.getInstance(context) }
    val syncManager = remember { GoogleDriveSyncManager.getInstance(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: History
    var watchlistItems by remember { mutableStateOf<List<WatchlistItem>>(emptyList()) }
    var historyItems by remember { mutableStateOf<List<WatchHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    val googleAccount = remember { syncManager.getSignedInAccount() }

    fun refreshData() {
        scope.launch {
            watchlistItems = dbHelper.getWatchlist()
            historyItems = dbHelper.getWatchHistory()
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        watchlistItems = dbHelper.getWatchlist()
        historyItems = dbHelper.getWatchHistory()
        isLoading = false

        // Auto-sync seamlessly with Google Drive if user is signed in
        if (googleAccount != null) {
            isSyncing = true
            syncManager.syncNow()
            refreshData()
            isSyncing = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        // Screen Header with Title & Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Library",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Google Drive Sync Indicator / Quick Sync
            if (googleAccount != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            isSyncing = true
                            val res = syncManager.syncNow()
                            refreshData()
                            isSyncing = false
                            syncMessage = if (res.isSuccess) "Synced with Cloud!" else "Sync failed"
                            kotlinx.coroutines.delay(2000)
                            syncMessage = null
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentCyan.copy(alpha = 0.15f))
                        .size(36.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = "Sync",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Sync Message Feedback Pill
        syncMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = msg,
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Google Drive Cloud Sync Banner (Seamless Cross-Platform)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (googleAccount != null) AccentCyan.copy(alpha = 0.3f) else DarkBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (googleAccount != null) AccentCyan.copy(alpha = 0.2f) else DarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cloud,
                            contentDescription = null,
                            tint = if (googleAccount != null) AccentCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (googleAccount != null) "Cloud Sync Connected" else "Google Drive Sync",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (googleAccount != null) "${googleAccount.email}" else "Sync Favorites & History with Desktop",
                            color = if (googleAccount != null) AccentCyan else TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (googleAccount != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isSyncing = true
                                    syncManager.syncNow()
                                    refreshData()
                                    isSyncing = false
                                }
                            },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.25f), contentColor = AccentCyan),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(text = if (isSyncing) "Syncing..." else "Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs: Favorites vs Watch History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Favorites Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 0) PrimaryViolet else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = if (selectedTab == 0) Color.White else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Favorites (${watchlistItems.size})",
                        color = if (selectedTab == 0) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // History Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 1) PrimaryViolet else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = if (selectedTab == 1) Color.White else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "History (${historyItems.size})",
                        color = if (selectedTab == 1) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (selectedTab == 0) {
            // ================= FAVORITES (WATCHLIST) TAB =================
            if (watchlistItems.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "Your Watchlist is Empty",
                    subtitle = "Explore anime and bookmark series to sync them across your devices."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
                            width = 110,
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
        } else {
            // ================= WATCH HISTORY TAB (YouTube Style) =================
            if (historyItems.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.History,
                    title = "No Watch History Yet",
                    subtitle = "Anime you watch will appear here with your exact timestamp so you can pick up where you left off."
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // History Toolbar with Clear All Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Watched",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = {
                                scope.launch {
                                    dbHelper.clearWatchHistory()
                                    refreshData()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = AccentRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear History", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historyItems, key = { it.animeId }) { item ->
                            YouTubeHistoryCard(
                                item = item,
                                onClick = {
                                    onAnimeClick(
                                        Anime(
                                            id = item.animeId,
                                            title = item.title,
                                            coverImage = item.coverUrl
                                        )
                                    )
                                },
                                onDelete = {
                                    scope.launch {
                                        dbHelper.deleteHistoryItem(item.animeId)
                                        refreshData()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * YouTube-style Watch History Card with exact timestamp progress bar,
 * thumbnail, and direct resume tap.
 */
@Composable
fun YouTubeHistoryCard(
    item: WatchHistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (item.durationMs > 0) {
        (item.positionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val percent = (progress * 100).toInt()
    val dateStr = remember(item.updatedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(item.updatedAt))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Duration & Progress Bar (YouTube Style)
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
            ) {
                AsyncImage(
                    model = item.coverUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // YouTube-style Red/Violet Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = PrimaryViolet,
                    trackColor = Color.Black.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Episode ${item.episodeNumber}",
                    color = PrimaryViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Exact Timestamp Progress
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${NowPlayingController.formatTimestamp(item.positionMs)} / ${NowPlayingController.formatTimestamp(item.durationMs)}",
                        color = AccentAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = " • ", color = TextMuted, fontSize = 10.sp)
                    Text(text = "$percent%", color = TextMuted, fontSize = 10.sp)
                    Text(text = " • ", color = TextMuted, fontSize = 10.sp)
                    Text(text = dateStr, color = TextMuted, fontSize = 10.sp)
                }
            }

            // Remove from History Button
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryViolet,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
