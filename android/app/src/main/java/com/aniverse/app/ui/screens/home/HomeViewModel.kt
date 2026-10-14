package com.aniverse.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.WatchHistoryItem
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.remote.AniListApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroAnime: Anime? = null,
    val continueWatching: List<WatchHistoryItem> = emptyList(),
    val top10Today: List<Anime> = emptyList(),
    val top10Week: List<Anime> = emptyList(),
    val top10Month: List<Anime> = emptyList(),
    val topAiring: List<Anime> = emptyList(),
    val mostPopular: List<Anime> = emptyList(),
    val mostWatched: List<Anime> = emptyList(),
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
                val historyDeferred = async { dbHelper.getWatchHistory() }
                val top10TodayDeferred = async { api.getTop10Today() }
                val top10WeekDeferred = async { api.getTop10Week() }
                val top10MonthDeferred = async { api.getTop10Month() }
                val topAiringDeferred = async { api.getTopAiring(page = 1, perPage = 20) }
                val mostPopularDeferred = async { api.getMostPopular(page = 1, perPage = 20) }
                val mostWatchedDeferred = async { api.getMostWatched(page = 1, perPage = 20) }
                val donghuaDeferred = async { api.searchAnime(keyword = "", isDonghua = true) }

                val history = historyDeferred.await()
                val top10Today = top10TodayDeferred.await()
                val top10Week = top10WeekDeferred.await()
                val top10Month = top10MonthDeferred.await()
                val topAiring = topAiringDeferred.await()
                val mostPopular = mostPopularDeferred.await()
                val mostWatched = mostWatchedDeferred.await()
                val donghua = donghuaDeferred.await()

                val hero = top10Today.firstOrNull() ?: mostPopular.firstOrNull()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    heroAnime = hero,
                    continueWatching = history,
                    top10Today = top10Today,
                    top10Week = top10Week,
                    top10Month = top10Month,
                    topAiring = topAiring,
                    mostPopular = mostPopular,
                    mostWatched = mostWatched,
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
