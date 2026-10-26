package com.gcoded.meditrack

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.domain.model.ThemeMode
import com.gcoded.meditrack.ui.AppUiState
import com.gcoded.meditrack.ui.AppViewModel
import com.gcoded.meditrack.ui.MediTrackRoot
import com.gcoded.meditrack.ui.theme.MediTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate(). Keeps the system splash until preferences are loaded.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { appViewModel.uiState.value is AppUiState.Loading }

        enableEdgeToEdge()
        setContent {
            val state by appViewModel.uiState.collectAsStateWithLifecycle()
            val preferences = (state as? AppUiState.Ready)?.preferences ?: return@setContent

            val darkTheme = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Keep the system bar icons readable when the in-app theme differs from the system one.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme }
                )
                onDispose {}
            }

            MediTrackTheme(
                darkTheme = darkTheme,
                dynamicColor = preferences.useDynamicColor
            ) {
                MediTrackRoot(
                    preferences = preferences,
                    onCompleteOnboarding = appViewModel::completeOnboarding
                )
            }
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}