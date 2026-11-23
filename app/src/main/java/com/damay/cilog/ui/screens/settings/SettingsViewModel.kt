package com.damay.cilog.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damay.cilog.data.local.UserPreferencesRepository
import com.damay.cilog.ui.theme.AppLanguageMode
import com.damay.cilog.ui.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppThemeMode.DARK
        )

    val languageMode: StateFlow<AppLanguageMode> = preferencesRepository.languageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguageMode.SYSTEM
        )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguageMode(mode: AppLanguageMode) {
        viewModelScope.launch {
            preferencesRepository.setLanguageMode(mode)

            // Update app locales dynamically without restarting app (F-09, N-01)
            val appLocale = when (mode) {
                AppLanguageMode.EN -> LocaleListCompat.forLanguageTags("en")
                AppLanguageMode.ID -> LocaleListCompat.forLanguageTags("in")
                AppLanguageMode.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
