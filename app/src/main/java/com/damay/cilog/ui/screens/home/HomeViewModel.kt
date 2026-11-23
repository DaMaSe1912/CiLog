package com.damay.cilog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.damay.cilog.data.local.UserPreferencesRepository
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.remote.MovieCategory
import com.damay.cilog.data.repository.HomeOverviewData
import com.damay.cilog.data.repository.MovieRepository
import com.damay.cilog.ui.theme.AppLanguageMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Default to ALL category (Disney+ / LK21 multi-section home view)
    private val _selectedCategory = MutableStateFlow(MovieCategory.ALL)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    val languageMode: StateFlow<AppLanguageMode> = preferencesRepository.languageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguageMode.SYSTEM
        )

    // Data for "ALL" multi-carousel view
    val homeOverview: StateFlow<HomeOverviewData> = languageMode.flatMapLatest { lang ->
        movieRepository.getHomeOverview(lang.apiCode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeOverviewData()
    )

    // Paging stream for specific selected category (Vertical infinite grid)
    val movies: Flow<PagingData<Movie>> = combine(
        _selectedCategory,
        languageMode
    ) { category, lang ->
        Pair(category, lang.apiCode)
    }.flatMapLatest { (category, apiCode) ->
        val queryCategory = if (category == MovieCategory.ALL) MovieCategory.POPULAR else category
        movieRepository.getMoviesStream(queryCategory, apiCode)
    }.cachedIn(viewModelScope)

    fun onCategorySelected(category: MovieCategory) {
        _selectedCategory.value = category
    }
}
