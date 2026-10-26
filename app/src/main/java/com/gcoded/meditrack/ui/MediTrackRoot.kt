package com.gcoded.meditrack.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.gcoded.meditrack.domain.model.UserPreferences
import com.gcoded.meditrack.ui.navigation.MediTrackNavHost
import com.gcoded.meditrack.ui.screens.onboarding.OnboardingScreen
import com.gcoded.meditrack.ui.screens.splash.BrandedSplash

private enum class RootPhase { SPLASH, ONBOARDING, MAIN }

/** Splash -> (first launch only) onboarding -> main app, with an animated transition. */
@Composable
fun MediTrackRoot(
    preferences: UserPreferences,
    onCompleteOnboarding: (String) -> Unit
) {
    // Saved across configuration changes and process death: the splash plays once per session.
    var splashFinished by rememberSaveable { mutableStateOf(false) }

    val phase = when {
        !splashFinished -> RootPhase.SPLASH
        !preferences.onboardingCompleted -> RootPhase.ONBOARDING
        else -> RootPhase.MAIN
    }

    AnimatedContent(
        targetState = phase,
        transitionSpec = {
            (fadeIn(tween(500, delayMillis = 100)) +
                scaleIn(animationSpec = tween(500, delayMillis = 100), initialScale = 0.94f)) togetherWith
                (fadeOut(tween(300)) + scaleOut(animationSpec = tween(300), targetScale = 1.06f))
        },
        label = "rootPhase"
    ) { target ->
        when (target) {
            RootPhase.SPLASH -> BrandedSplash(onFinished = { splashFinished = true })
            RootPhase.ONBOARDING -> OnboardingScreen(onContinue = onCompleteOnboarding)
            RootPhase.MAIN -> MediTrackNavHost()
        }
    }
}