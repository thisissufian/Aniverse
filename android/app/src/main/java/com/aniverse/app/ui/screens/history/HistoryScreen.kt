package com.aniverse.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.remote.GoogleDriveSyncManager
import com.aniverse.app.ui.theme.*
import com.aniverse.app.util.NowPlayingController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { AniverseDbHelper.getInstance(context) }
    val syncManager = remember { GoogleDriveSyncManager.getInstance(context) }

    var historyItems by remember { mutableStateOf<List<WatchHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    val googleAccount = remember { syncManager.getSignedInAccount() }

    fun refreshHistory() {
        scope.launch {
            historyItems = dbHelper.getWatchHistory()
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        historyItems = dbHelper.getWatchHistory()
        isLoading = false

        if (googleAccount != null) {
            isSyncing = true
            syncManager.syncNow()
            refreshHistory()
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
                text = "Watch History",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Cloud Sync Indicator
            if (googleAccount != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            isSyncing = true
                            val res = syncManager.syncNow()
                            refreshHistory()
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

        // Google Drive Cloud Sync Card
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
                            text = if (googleAccount != null) "${googleAccount.email}" else "Sync exact seconds with Desktop app",
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
                                    refreshHistory()
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

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (historyItems.isEmpty()) {
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
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = PrimaryViolet,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Watch History Yet",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Watched anime will appear here with exact timestamps so you can resume anytime.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar with item count and Clear All button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${historyItems.size} watched series",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(
                        onClick = {
                            scope.launch {
                                dbHelper.clearWatchHistory()
                                refreshHistory()
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAnimeClick(
                                        Anime(
                                            id = item.animeId,
                                            title = item.title,
                                            coverImage = item.coverUrl
                                        )
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail with Duration Badge & YouTube-style Progress Line
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
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

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

                                // Delete single item button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            dbHelper.deleteHistoryItem(item.animeId)
                                            refreshHistory()
                                        }
                                    },
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
                }
            }
        }
    }
}
