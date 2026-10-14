package com.aniverse.app.ui.screens.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.WatchlistItem
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.remote.GoogleDriveSyncManager
import com.aniverse.app.ui.components.AnimeCard
import com.aniverse.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WatchlistScreen(
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { AniverseDbHelper.getInstance(context) }
    val syncManager = remember { GoogleDriveSyncManager.getInstance(context) }

    var watchlistItems by remember { mutableStateOf<List<WatchlistItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    val googleAccount = remember { syncManager.getSignedInAccount() }

    fun refreshWatchlist() {
        scope.launch {
            watchlistItems = dbHelper.getWatchlist()
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        watchlistItems = dbHelper.getWatchlist()
        isLoading = false

        if (googleAccount != null) {
            isSyncing = true
            syncManager.syncNow()
            refreshWatchlist()
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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Watchlist (${watchlistItems.size})",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            if (googleAccount != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            isSyncing = true
                            val res = syncManager.syncNow()
                            refreshWatchlist()
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

        syncMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = msg, color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Google Drive Cloud Sync Banner
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
                    .border(
                        1.dp,
                        if (googleAccount != null) AccentCyan.copy(alpha = 0.3f) else DarkBorder,
                        RoundedCornerShape(14.dp)
                    )
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
                            text = if (googleAccount != null) "${googleAccount.email}" else "Sync Favorites with Desktop app",
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
                                    refreshWatchlist()
                                    isSyncing = false
                                }
                            },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentCyan.copy(alpha = 0.25f),
                                contentColor = AccentCyan
                            ),
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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (watchlistItems.isEmpty()) {
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
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = PrimaryViolet,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Watchlist is Empty",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Bookmark anime series to track and sync them across all your devices.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
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
    }
}
