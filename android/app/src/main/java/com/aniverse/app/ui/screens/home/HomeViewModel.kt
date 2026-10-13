package com.aniverse.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.WatchHistoryItem
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.remote.AniListApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroAnime: Anime? = null,
    val continueWatching: List<WatchHistoryItem> = emptyList(),
    val latestEpisodes: List<Anime> = emptyList(),
    val trendingAnime: List<Anime> = emptyList(),
    val popularAnime: List<Anime> = emptyList(),
    val donghuaAnime: List<Anime> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val api: AniListApi = AniListApi()
    private val dbHelper: AniverseDbHelper = AniverseDbHelper.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val history = dbHelper.getWatchHistory()
                val latest = api.getAiringSchedule(page = 1, perPage = 20)
                val trending = api.getTrendingAnime(page = 1, perPage = 20)
                val popular = api.getPopularAnime(page = 1, perPage = 20)
                val donghua = api.searchAnime(keyword = "", isDonghua = true)

                val hero = trending.firstOrNull() ?: popular.firstOrNull()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    heroAnime = hero,
                    continueWatching = history,
                    latestEpisodes = latest,
                    trendingAnime = trending,
                    popularAnime = popular,
                    donghuaAnime = donghua
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load anime feed"
                )
            }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            val history = dbHelper.getWatchHistory()
            _uiState.value = _uiState.value.copy(continueWatching = history)
        }
    }
}
