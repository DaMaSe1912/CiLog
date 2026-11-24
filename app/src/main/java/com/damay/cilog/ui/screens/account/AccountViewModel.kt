package com.damay.cilog.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damay.cilog.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
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
            started = SharingStarted.Eagerly,
            initialValue = authRepository.currentUser
        )

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    private val _event = MutableSharedFlow<AccountEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<AccountEvent> = _event.asSharedFlow()

    fun refreshUser() {
        viewModelScope.launch {
            authRepository.reloadCurrentUser()
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount(password: String? = null) {
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteError.value = null
            authRepository.deleteAccount(password)
                .onSuccess {
                    _isDeleting.value = false
                    _deleteError.value = null
                    _event.emit(AccountEvent.AccountDeleted)
                }
                .onFailure { error ->
                    _isDeleting.value = false
                    val errorMsg = mapDeleteError(error)
                    _deleteError.value = errorMsg
                    _event.emit(AccountEvent.ShowToast(errorMsg))
                }
        }
    }

    fun clearDeleteError() {
        _deleteError.value = null
    }

    private fun mapDeleteError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException -> {
                "Kata sandi salah. Periksa kembali kata sandi akun Anda."
            }
            is FirebaseAuthRecentLoginRequiredException -> {
                "Sesi telah kedaluwarsa. Masukkan kata sandi Anda untuk mengonfirmasi penghapusan akun."
            }
            is TimeoutCancellationException -> {
                "Waktu permintaan habis. Periksa koneksi internet Anda dan coba lagi."
            }
            else -> {
                val msg = error.localizedMessage.orEmpty()
                if (msg.contains("recent", ignoreCase = true) || msg.contains("CREDENTIAL_TOO_OLD", ignoreCase = true)) {
                    "Masukkan kata sandi Anda untuk mengonfirmasi penghapusan akun."
                } else {
                    error.localizedMessage ?: "Gagal menghapus akun. Silakan coba lagi."
                }
            }
        }
    }
}
