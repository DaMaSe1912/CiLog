package com.damay.cilog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.damay.cilog.data.local.UserPreferencesRepository
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.remote.MovieCategory
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

    private val _selectedCategory = MutableStateFlow(MovieCategory.POPULAR)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    val languageMode: StateFlow<AppLanguageMode> = preferencesRepository.languageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguageMode.SYSTEM
        )

    val movies: Flow<PagingData<Movie>> = combine(
        _selectedCategory,
        languageMode
    ) { category, lang ->
        Pair(category, lang.apiCode)
    }.flatMapLatest { (category, apiCode) ->
        movieRepository.getMoviesStream(category, apiCode)
    }.cachedIn(viewModelScope)

    fun onCategorySelected(category: MovieCategory) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
        }
    }
}
