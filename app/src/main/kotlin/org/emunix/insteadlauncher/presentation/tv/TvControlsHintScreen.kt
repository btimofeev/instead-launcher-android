/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R

@Composable
fun TvControlsHintScreen(
    gameName: String,
    onBackClick: () -> Unit,
) {
    val viewModel: TvControlsHintViewModel = hiltViewModel()

    TvControlsHintScreenContent(
        onGotItClick = {
            viewModel.closeAndStartGame(gameName)
            onBackClick()
        },
        onDontShowAgainClick = {
            viewModel.disableAndStartGame(gameName)
            onBackClick()
        },
    )
}

@Composable
private fun TvControlsHintScreenContent(
    onGotItClick: () -> Unit,
    onDontShowAgainClick: () -> Unit,
) {
    val gotItFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        gotItFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 40.dp),
    ) {
        Text(
            text = stringResource(R.string.tv_controls_hint_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        TvControlsHintText(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                modifier = Modifier.focusRequester(gotItFocus),
                onClick = onGotItClick,
            ) {
                Text(text = stringResource(R.string.tv_controls_hint_got_it))
            }
            Button(onClick = onDontShowAgainClick) {
                Text(text = stringResource(R.string.tv_controls_hint_dont_show_again))
            }
        }
    }
}

@Composable
private fun TvControlsHintText(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val scrollStep = with(LocalDensity.current) { 200.dp.toPx() }
    var isFocused by remember { mutableStateOf(false) }
    val focusBorderAlpha by animateFloatAsState(targetValue = if (isFocused) 1f else 0f)
    val borderColor = Color.White
    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onPreviewKeyEvent { event ->
                when (event.key) {
                    Key.DirectionDown -> {
                        if (!scrollState.canScrollForward) {
                            false
                        } else {
                            if (event.type == KeyEventType.KeyDown) {
                                scope.launch { scrollState.animateScrollBy(scrollStep) }
                            }
                            true
                        }
                    }

                    Key.DirectionUp -> {
                        if (!scrollState.canScrollBackward) {
                            false
                        } else {
                            if (event.type == KeyEventType.KeyDown) {
                                scope.launch { scrollState.animateScrollBy(-scrollStep) }
                            }
                            true
                        }
                    }

                    else -> false
                }
            }
            .verticalScroll(scrollState)
            .drawBehind {
                if (focusBorderAlpha > 0f) {
                    val strokeWidth = 2.dp.toPx()
                    val inset = strokeWidth / 2
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        cornerRadius = CornerRadius(8.dp.toPx() - inset),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth),
                        alpha = focusBorderAlpha,
                    )
                }
            },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HintParagraph(text = stringResource(R.string.tv_controls_hint_intro))
            HintParagraph(text = stringResource(R.string.tv_controls_hint_modes))
            HintBullet(text = stringResource(R.string.tv_controls_hint_links_mode))
            HintBullet(text = stringResource(R.string.tv_controls_hint_cursor_mode))
            HintParagraph(text = stringResource(R.string.tv_controls_hint_fallback))
            HintParagraph(text = stringResource(R.string.tv_controls_hint_back_short))
            HintParagraph(text = stringResource(R.string.tv_controls_hint_back_long))
            HintParagraph(text = stringResource(R.string.tv_controls_hint_menu))
        }
    }
}

@Composable
private fun HintParagraph(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun HintBullet(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "•", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
