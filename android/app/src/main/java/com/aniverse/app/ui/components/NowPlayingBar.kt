package com.aniverse.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aniverse.app.ui.theme.*
import com.aniverse.app.util.NowPlayingController

@Composable
fun NowPlayingBar(
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = NowPlayingController.isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val progress = if (NowPlayingController.durationMs > 0) {
            (NowPlayingController.positionMs.toFloat() / NowPlayingController.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val title = NowPlayingController.englishTitle ?: NowPlayingController.animeTitle

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.98f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryViolet.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { onBarClick() }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Accent Progress Bar (Spotify-like)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = PrimaryViolet,
                        trackColor = DarkBorder.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Poster Thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCard)
                        ) {
                            if (NowPlayingController.coverUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = NowPlayingController.coverUrl,
                                    contentDescription = title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (NowPlayingController.isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryViolet)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Anime Title and Episode Progress
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "EP ${NowPlayingController.episodeNumber}",
                                    color = PrimaryViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = " • ",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Text(
                                    text = "${NowPlayingController.formatTimestamp(NowPlayingController.positionMs)} / ${NowPlayingController.formatTimestamp(NowPlayingController.durationMs)}",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play / Pause Button
                        IconButton(
                            onClick = { NowPlayingController.togglePlayPause() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryViolet.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (NowPlayingController.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (NowPlayingController.isPlaying) "Pause" else "Play",
                                tint = PrimaryViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = { NowPlayingController.dismiss() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
