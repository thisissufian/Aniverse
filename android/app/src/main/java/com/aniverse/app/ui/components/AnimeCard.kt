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
    width: Int? = 135
) {
    val cardModifier = if (width != null) {
        modifier.width(width.dp)
    } else {
        modifier.fillMaxWidth()
    }

    Column(
        modifier = cardModifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // Poster Box
        val posterModifier = if (width != null) {
            Modifier
                .width(width.dp)
                .height((width * 1.45).dp)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
        }

        Box(
            modifier = posterModifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
        ) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient overlay for readability of bottom tags
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

            // Top Badges Row: Format Tag (TV / Movie) and Status Tag (Ongoing / Completed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Format Tag: "TV" / "Movie" / "OVA" / "Donghua"
                val formatLabel = when {
                    anime.isDonghua -> "Donghua"
                    anime.format == "MOVIE" -> "Movie"
                    anime.format == "TV" || anime.format == "TV_SHORT" -> "TV"
                    anime.format == "OVA" -> "OVA"
                    anime.format == "ONA" -> "ONA"
                    anime.format == "SPECIAL" -> "Special"
                    else -> anime.format ?: "TV"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(PrimaryViolet.copy(alpha = 0.9f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatLabel,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Status Tag: "Ongoing" vs "Completed"
                val isOngoing = anime.status == "RELEASING"
                val isUpcoming = anime.status == "NOT_YET_RELEASED"
                val (statusText, statusBg, statusTextColor) = when {
                    isOngoing -> Triple("Ongoing", AccentGreen.copy(alpha = 0.9f), Color.White)
                    isUpcoming -> Triple("Upcoming", AccentAmber.copy(alpha = 0.9f), Color.White)
                    else -> Triple("Completed", DarkSurface.copy(alpha = 0.85f), TextSecondary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(statusBg)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Badges Row: Audio Tag ("SUB & DUB" vs "SUB") and Score
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sub & Dub Tag
                val hasDub = when {
                    anime.dubCount != null -> anime.dubCount > 0
                    anime.isDonghua -> false
                    anime.format == "TV" -> true
                    else -> true
                }
                val audioLabel = if (hasDub) "SUB & DUB" else "SUB"
                val audioBg = if (hasDub) AccentAmber.copy(alpha = 0.9f) else DarkSurface.copy(alpha = 0.85f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(audioBg)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = audioLabel,
                        color = if (hasDub) Color.Black else TextPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Rating Badge
                anime.averageScore?.let { score ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkCanvas.copy(alpha = 0.85f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ ${(score / 10f)}",
                            color = AccentAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title at the bottom of the card
        Text(
            text = anime.englishTitle ?: anime.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp
        )
    }
}
