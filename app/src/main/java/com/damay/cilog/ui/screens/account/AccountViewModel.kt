package com.damay.cilog.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damay.cilog.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
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

sealed interface AccountEvent {
    data class ShowToast(val message: String) : AccountEvent
    data object AccountDeleted : AccountEvent
    data object RequireRecentLogin : AccountEvent
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentUser
        )

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _event = MutableSharedFlow<AccountEvent>()
    val event: SharedFlow<AccountEvent> = _event.asSharedFlow()

    fun refreshUser() {
        viewModelScope.launch {
            authRepository.reloadCurrentUser()
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isDeleting.value = true
            authRepository.deleteAccount()
                .onSuccess {
                    _isDeleting.value = false
                    _event.emit(AccountEvent.AccountDeleted)
                }
                .onFailure { error ->
                    _isDeleting.value = false
                    if (error is FirebaseAuthRecentLoginRequiredException) {
                        _event.emit(AccountEvent.RequireRecentLogin)
                    } else {
                        _event.emit(AccountEvent.ShowToast(error.localizedMessage ?: "Failed to delete account"))
                    }
                }
        }
    }
}
