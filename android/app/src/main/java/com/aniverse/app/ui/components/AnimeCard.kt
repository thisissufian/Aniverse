package com.aniverse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun AnimeCard(
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 135
) {
    Column(
        modifier = modifier
            .width(width.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // Poster Box
        Box(
            modifier = Modifier
                .width(width.dp)
                .height((width * 1.45).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
        ) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient shadow on poster bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                DarkCanvas.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Badge
                anime.averageScore?.let { score ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCanvas.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ ${(score / 10f)}",
                            color = AccentAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Format or Donghua Badge
                val badgeText = if (anime.isDonghua) "DONGHUA" else (anime.format ?: "TV")
                val badgeColor = if (anime.isDonghua) AccentCyan else PrimaryViolet

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Bottom Badges (Episode Count / Sub-Dub)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                anime.episodes?.let { eps ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurface.copy(alpha = 0.9f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EP $eps",
                            color = TextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = anime.englishTitle ?: anime.title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp
        )
    }
}
