package com.gcoded.meditrack.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gcoded.meditrack.R
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1900L

/**
 * In-app splash shown right after the system splash. The system splash can only show an icon,
 * so the app name and the subtitles at the bottom are animated here.
 * Colors come from resources (not the app theme) so they match the system splash exactly.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrandedSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        visible = true
        delay(SPLASH_DURATION_MS)
        currentOnFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_background))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + scaleIn(animationSpec = tween(600), initialScale = 0.8f)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_splash),
                    contentDescription = null,
                    modifier = Modifier.size(220.dp)
                )
            }
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.offset(y = (-36).dp),
                enter = fadeIn(tween(500, delayMillis = 350)) +
                    slideInVertically(
                        animationSpec = tween(500, delayMillis = 350),
                        initialOffsetY = { it / 2 }
                    )
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = colorResource(R.color.splash_icon)
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 32.dp, end = 32.dp, bottom = 48.dp),
            enter = fadeIn(tween(600, delayMillis = 700)) +
                slideInVertically(
                    animationSpec = tween(600, delayMillis = 700),
                    initialOffsetY = { it }
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.splash_text),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.splash_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.splash_text_muted),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}