package com.damay.cilog.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damay.cilog.data.local.UserPreferencesRepository
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.model.MovieDetail
import com.damay.cilog.data.repository.AuthRepository
import com.damay.cilog.data.repository.FavoritesRepository
import com.damay.cilog.data.repository.MovieRepository
import com.damay.cilog.ui.theme.AppLanguageMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val detail: MovieDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

sealed interface DetailEvent {
    data object RequireAuth : DetailEvent
    data class ShowMessage(val messageRes: Int) : DetailEvent
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository,
    preferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"]).toString().toInt()

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<DetailEvent>()
    val event: SharedFlow<DetailEvent> = _event.asSharedFlow()

    val isFavorite: StateFlow<Boolean> = favoritesRepository.isFavorite(movieId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val languageMode: StateFlow<AppLanguageMode> = preferencesRepository.languageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppLanguageMode.SYSTEM
        )

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            movieRepository.getMovieDetail(movieId, languageMode.value.apiCode)
                .collect { result ->
                    result.onSuccess { detail ->
                        _uiState.value = DetailUiState.Success(detail)
                    }.onFailure { error ->
                        _uiState.value = DetailUiState.Error(error.localizedMessage ?: "Unknown error")
                    }
                }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                val detail = currentState.detail
                val movie = Movie(
                    id = detail.id,
                    title = detail.title,
                    overview = detail.overview,
                    posterUrl = detail.posterUrl,
                    backdropUrl = detail.backdropUrl,
                    releaseDate = detail.releaseDate,
                    voteAverage = detail.voteAverage,
                    voteCount = 0
                )

                if (isFavorite.value) {
                    favoritesRepository.removeFavorite(detail.id)
                    _event.emit(DetailEvent.ShowMessage(com.damay.cilog.R.string.favorite_removed))
                } else {
                    favoritesRepository.addFavorite(movie)
                    _event.emit(DetailEvent.ShowMessage(com.damay.cilog.R.string.favorite_added))
                }
            }
        }
    }
}
