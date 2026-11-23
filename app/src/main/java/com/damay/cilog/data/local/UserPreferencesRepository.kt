package com.damay.cilog.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.damay.cilog.ui.theme.AppLanguageMode
import com.damay.cilog.ui.theme.AppThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cilog_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.THEME_MODE]) {
            AppThemeMode.SYSTEM.name -> AppThemeMode.SYSTEM
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }

    val languageMode: Flow<AppLanguageMode> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.LANGUAGE_MODE]) {
            AppLanguageMode.EN.name -> AppLanguageMode.EN
            AppLanguageMode.ID.name -> AppLanguageMode.ID
            else -> AppLanguageMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguageMode(mode: AppLanguageMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_MODE] = mode.name
        }
    }
}
