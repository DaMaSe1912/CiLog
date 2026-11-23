package com.damay.cilog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.damay.cilog.data.local.UserPreferencesRepository
import com.damay.cilog.ui.MainScreen
import com.damay.cilog.ui.theme.AppThemeMode
import com.damay.cilog.ui.theme.CilogTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by preferencesRepository.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)

            CilogTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}
