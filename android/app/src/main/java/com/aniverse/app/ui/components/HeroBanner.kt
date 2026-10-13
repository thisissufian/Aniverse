package com.aniverse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.aniverse.app.data.model.Anime
import com.aniverse.app.ui.theme.*

@Composable
fun HeroBanner(
    anime: Anime?,
    onWatchClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    if (anime == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .background(DarkCanvas)
    ) {
        // Banner or Cover Image
        AsyncImage(
            model = anime.bannerImage ?: anime.coverImage,
            contentDescription = anime.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Seamless Fade Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkCanvas.copy(alpha = 0.5f),
                            DarkCanvas.copy(alpha = 0.9f),
                            DarkCanvas
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Genre Chips & Score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                anime.averageScore?.let { score ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface.copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${score}%",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                anime.genres.take(2).forEach { genre ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCard.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = genre,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = anime.englishTitle ?: anime.title,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Watch Button
            Button(
                onClick = { onWatchClick(anime) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Watch Now",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
