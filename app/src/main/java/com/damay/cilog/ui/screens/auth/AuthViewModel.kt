package com.damay.cilog.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damay.cilog.data.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface AuthEvent {
    data object AuthSuccess : AuthEvent
    data class ShowToast(val message: String) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<AuthEvent> = _event.asSharedFlow()

    fun signInWithEmail(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (!validateInputs(trimmedEmail, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signInWithEmail(trimmedEmail, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                    _event.emit(AuthEvent.AuthSuccess)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    fun signUpWithEmail(name: String, email: String, password: String, confirmPassword: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isBlank()) {
            _uiState.value = AuthUiState.Error("Nama lengkap tidak boleh kosong.")
            return
        }

        if (!validateInputs(trimmedEmail, password)) return

        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Konfirmasi kata sandi tidak cocok.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signUpWithEmail(trimmedName, trimmedEmail, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                    _event.emit(AuthEvent.AuthSuccess)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signInWithGoogleIdToken(idToken)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                    _event.emit(AuthEvent.AuthSuccess)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = AuthUiState.Error("Masukkan alamat email yang valid.")
            return
        }

        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(trimmedEmail)
                .onSuccess {
                    _event.emit(AuthEvent.ShowToast("Tautan atur ulang kata sandi telah dikirim ke $trimmedEmail"))
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Masukkan alamat email yang valid.")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Kata sandi minimal 6 karakter.")
            return false
        }
        return true
    }

    private fun mapFirebaseError(error: Throwable): String {
        return when (error) {
            is TimeoutCancellationException -> {
                "Waktu permintaan habis (timeout). Silakan periksa koneksi internet Anda dan coba lagi."
            }
            is FirebaseAuthWeakPasswordException -> {
                "Kata sandi terlalu lemah. Gunakan minimal 6 karakter kombinasi huruf dan angka."
            }
            is FirebaseAuthUserCollisionException -> {
                "Alamat email ini sudah terdaftar. Silakan gunakan email lain atau langsung masuk."
            }
            is FirebaseAuthInvalidCredentialsException -> {
                "Email atau kata sandi tidak valid. Periksa kembali email dan kata sandi Anda."
            }
            is FirebaseAuthInvalidUserException -> {
                "Akun dengan email ini tidak ditemukan atau telah dinonaktifkan."
            }
            is FirebaseNetworkException -> {
                "Gagal terhubung ke jaringan. Periksa koneksi internet perangkat Anda."
            }
            is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> {
                        "Metode login Email/Password belum diaktifkan di Firebase Console proyek ini. Silakan aktifkan di menu Firebase Console > Authentication > Sign-in method > Email/Password."
                    }
                    "ERROR_TOO_MANY_REQUESTS" -> {
                        "Terlalu banyak percobaan gagal. Silakan coba lagi beberapa saat lagi."
                    }
                    "ERROR_INVALID_EMAIL" -> {
                        "Format alamat email tidak valid."
                    }
                    "ERROR_WRONG_PASSWORD" -> {
                        "Kata sandi yang Anda masukkan salah."
                    }
                    "ERROR_USER_NOT_FOUND" -> {
                        "Akun dengan email tersebut tidak ditemukan."
                    }
                    else -> error.localizedMessage ?: "Terjadi kendala autentikasi (${error.errorCode})"
                }
            }
            else -> error.localizedMessage ?: "Terjadi kesalahan. Silakan coba lagi."
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
