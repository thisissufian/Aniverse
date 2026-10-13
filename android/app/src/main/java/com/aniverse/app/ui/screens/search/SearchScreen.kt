package com.aniverse.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.remote.AniListApi
import com.aniverse.app.ui.components.AnimeCard
import com.aniverse.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var isDonghuaFilter by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { AniListApi() }

    fun performSearch() {
        if (query.trim().isEmpty() && !isDonghuaFilter) return
        scope.launch {
            isSearching = true
            try {
                results = api.searchAnime(keyword = query.trim(), isDonghua = isDonghuaFilter)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSearching = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        // Search Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    performSearch()
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface),
                placeholder = {
                    Text("Search anime, movies, donghua...", color = TextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = PrimaryViolet)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.clickable {
                                query = ""
                                results = emptyList()
                            }
                        )
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryViolet,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryViolet
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isDonghuaFilter,
                onClick = {
                    isDonghuaFilter = false
                    performSearch()
                },
                label = { Text("All Anime", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryViolet,
                    selectedLabelColor = Color.White,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = isDonghuaFilter,
                onClick = {
                    isDonghuaFilter = true
                    performSearch()
                },
                label = { Text("Chinese Donghua", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan,
                    selectedLabelColor = Color.White,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results Grid
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (results.isEmpty() && query.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No anime found for \"$query\"", color = TextMuted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = results, key = { it.id }) { anime ->
                    AnimeCard(
                        anime = anime,
                        width = 110,
                        onClick = { onAnimeClick(anime) }
                    )
                }
            }
        }
    }
}
