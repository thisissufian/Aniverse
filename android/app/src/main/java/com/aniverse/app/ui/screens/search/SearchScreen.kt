package com.aniverse.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var selectedFormat by remember { mutableStateOf("ALL") }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var isDonghuaFilter by remember { mutableStateOf(false) }

    var showFilterSheet by remember { mutableStateOf(false) }

    var results by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { AniListApi() }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val allGenres = remember {
        listOf(
            "Action", "Adventure", "Comedy", "Drama", "Fantasy",
            "Romance", "Sci-Fi", "Slice of Life", "Supernatural",
            "Mystery", "Sports", "Horror", "Psychological",
            "Thriller", "Ecchi", "Mecha", "Music"
        )
    }

    val allStatuses = remember {
        listOf(
            "ALL" to "All",
            "RELEASING" to "Ongoing",
            "FINISHED" to "Completed",
            "NOT_YET_RELEASED" to "Upcoming"
        )
    }

    val allFormats = remember {
        listOf(
            "ALL" to "All",
            "TV" to "TV Series",
            "MOVIE" to "Movie",
            "OVA" to "OVA",
            "ONA" to "ONA",
            "SPECIAL" to "Special"
        )
    }

    val allYears = remember {
        listOf(null, 2026, 2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018, 2015, 2010, 2000, 1990)
    }

    val activeFilterCount = (if (selectedGenres.isNotEmpty()) selectedGenres.size else 0) +
            (if (selectedStatus != "ALL") 1 else 0) +
            (if (selectedFormat != "ALL") 1 else 0) +
            (if (selectedYear != null) 1 else 0) +
            (if (isDonghuaFilter) 1 else 0)

    fun executeSearch() {
        searchJob?.cancel()
        searchJob = scope.launch {
            isSearching = true
            try {
                val list = api.searchAnime(
                    keyword = query.trim().takeIf { it.isNotEmpty() },
                    genres = selectedGenres.toList(),
                    status = selectedStatus.takeIf { it != "ALL" },
                    seasonYear = selectedYear,
                    format = selectedFormat.takeIf { it != "ALL" },
                    isDonghua = isDonghuaFilter,
                    page = 1,
                    perPage = 30
                )
                results = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSearching = false
            }
        }
    }

    // Initial load: show trending/popular anime on screen launch
    LaunchedEffect(Unit) {
        if (results.isEmpty()) {
            executeSearch()
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
                    // Debounce search while typing
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        delay(400)
                        executeSearch()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface),
                placeholder = {
                    Text("Search anime, movies, series...", color = TextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = PrimaryViolet)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            executeSearch()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextMuted)
                        }
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

            Spacer(modifier = Modifier.width(10.dp))

            // Filter Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (activeFilterCount > 0) PrimaryViolet else DarkSurface)
                    .clickable { showFilterSheet = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filters",
                        tint = if (activeFilterCount > 0) Color.White else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (activeFilterCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$activeFilterCount",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Filter Pills Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // All vs Chinese Donghua switch
            item {
                FilterChip(
                    selected = isDonghuaFilter,
                    onClick = {
                        isDonghuaFilter = !isDonghuaFilter
                        executeSearch()
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

            // Quick Status Chips
            if (selectedStatus != "ALL") {
                item {
                    val statusLabel = allStatuses.find { it.first == selectedStatus }?.second ?: selectedStatus
                    FilterChip(
                        selected = true,
                        onClick = {
                            selectedStatus = "ALL"
                            executeSearch()
                        },
                        label = { Text("Status: $statusLabel ✕", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryVioletDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Quick Format Chip
            if (selectedFormat != "ALL") {
                item {
                    val formatLabel = allFormats.find { it.first == selectedFormat }?.second ?: selectedFormat
                    FilterChip(
                        selected = true,
                        onClick = {
                            selectedFormat = "ALL"
                            executeSearch()
                        },
                        label = { Text("Format: $formatLabel ✕", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryVioletDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Quick Year Chip
            if (selectedYear != null) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = {
                            selectedYear = null
                            executeSearch()
                        },
                        label = { Text("Year: $selectedYear ✕", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryVioletDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Selected Genres Pills
            items(selectedGenres.toList()) { genre ->
                FilterChip(
                    selected = true,
                    onClick = {
                        selectedGenres = selectedGenres - genre
                        executeSearch()
                    },
                    label = { Text("$genre ✕", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryViolet,
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (activeFilterCount > 0) {
                item {
                    Text(
                        text = "Clear All",
                        color = AccentRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                selectedGenres = emptySet()
                                selectedStatus = "ALL"
                                selectedFormat = "ALL"
                                selectedYear = null
                                isDonghuaFilter = false
                                executeSearch()
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3-Column Results Grid (Matches Screenshot 3)
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (query.isNotEmpty()) "No anime found for \"$query\"" else "No anime match selected filters",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = results, key = { it.id }) { anime ->
                    AnimeCard(
                        anime = anime,
                        width = null, // null width lets card dynamically adapt to 3 columns
                        onClick = { onAnimeClick(anime) }
                    )
                }
            }
        }
    }

    // Modal Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = DarkSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Anime",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Reset",
                            color = AccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                selectedGenres = emptySet()
                                selectedStatus = "ALL"
                                selectedFormat = "ALL"
                                selectedYear = null
                                isDonghuaFilter = false
                            }
                        )

                        Text(
                            text = "Apply",
                            color = PrimaryViolet,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                showFilterSheet = false
                                executeSearch()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Genres Section (Multi-select)
                Text(
                    text = "GENRES (MULTI-SELECT)",
                    color = PrimaryViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allGenres) { genre ->
                        val isSelected = selectedGenres.contains(genre)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedGenres = if (isSelected) {
                                    selectedGenres - genre
                                } else {
                                    selectedGenres + genre
                                }
                            },
                            label = { Text(genre, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryViolet,
                                selectedLabelColor = Color.White,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Status Section (Ongoing vs Completed)
                Text(
                    text = "STATUS",
                    color = PrimaryViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allStatuses) { (key, label) ->
                        val isSelected = selectedStatus == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatus = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (key == "RELEASING") AccentGreen else PrimaryViolet,
                                selectedLabelColor = Color.White,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Format Section (TV Series, Movie, OVA...)
                Text(
                    text = "FORMAT",
                    color = PrimaryViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allFormats) { (key, label) ->
                        val isSelected = selectedFormat == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFormat = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryViolet,
                                selectedLabelColor = Color.White,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Starting Year Section
                Text(
                    text = "STARTING YEAR",
                    color = PrimaryViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allYears) { year ->
                        val isSelected = selectedYear == year
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedYear = year },
                            label = { Text(if (year != null) "$year" else "All Years", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryViolet,
                                selectedLabelColor = Color.White,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        showFilterSheet = false
                        executeSearch()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
