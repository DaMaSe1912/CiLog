package com.damay.cilog.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.damay.cilog.data.local.UserPreferencesRepository
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.repository.MovieRepository
import com.damay.cilog.ui.theme.AppLanguageMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    preferencesRepository: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")

    val languageMode: StateFlow<AppLanguageMode> = preferencesRepository.languageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguageMode.SYSTEM
        )

    val searchResults: Flow<PagingData<Movie>> = combine(
        searchQuery.debounce(400).distinctUntilChanged(),
        languageMode
    ) { query, lang ->
        Pair(query.trim(), lang.apiCode)
    }.flatMapLatest { (query, apiCode) ->
        if (query.isBlank()) {
            emptyFlow()
        } else {
            movieRepository.searchMoviesStream(query, apiCode)
        }
    }.cachedIn(viewModelScope)

    fun onQueryChange(newQuery: String) {
        savedStateHandle[KEY_SEARCH_QUERY] = newQuery
    }

    fun onClearQuery() {
        savedStateHandle[KEY_SEARCH_QUERY] = ""
    }
}
