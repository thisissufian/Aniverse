package com.aniverse.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aniverse.app.data.model.Anime
import com.aniverse.app.ui.theme.PrimaryViolet
import com.aniverse.app.ui.theme.TextPrimary
import com.aniverse.app.ui.theme.TextSecondary

@Composable
fun AnimeSection(
    title: String,
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onSeeAllClick: (() -> Unit)? = null
) {
    if (animeList.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                subtitle?.let {
                    Text(
                        text = it,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            onSeeAllClick?.let {
                Text(
                    text = "See All",
                    color = PrimaryViolet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { it() }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = animeList, key = { it.id }) { anime ->
                AnimeCard(
                    anime = anime,
                    onClick = { onAnimeClick(anime) }
                )
            }
        }
    }
}
