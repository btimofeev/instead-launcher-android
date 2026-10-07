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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.presentation.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.presentation.dialogs.ErrorDialog
import org.emunix.insteadlauncher.presentation.game.GameViewModel
import org.emunix.insteadlauncher.presentation.models.GameInfoScreenState
import org.emunix.insteadlauncher.presentation.models.ProgressType

@Composable
fun TvGameInfoScreen(
    gameName: String,
    onBackClick: () -> Unit,
    onShowHintClick: () -> Unit,
) {
    val viewModel: GameViewModel = hiltViewModel()
    val hintViewModel: TvControlsHintViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val closeCommand by viewModel.closeScreenCommand.collectAsState(initial = null)
    val errorDialog by viewModel.showErrorDialog.collectAsState()
    val deleteGameName by viewModel.showDeleteGameDialog.collectAsState()

    LaunchedEffect(gameName) {
        viewModel.init(gameName)
    }

    if (closeCommand != null) {
        onBackClick()
    }

    TvGameInfoScreenContent(
        state = state,
        onInstallClick = viewModel::installGame,
        onRunClick = {
            if (hintViewModel.shouldShowGameControlsHint()) {
                onShowHintClick()
            } else {
                viewModel.runGame()
            }
        },
        onUpdateClick = viewModel::installGame,
        onDeleteClick = viewModel::onDeleteGameClicked,
        onCancelClick = viewModel::cancelInstallGame,
    )
    deleteGameName?.let { name ->
        DeleteGameDialog(
            onConfirm = { viewModel.onDeleteGameConfirmed(name) },
            onDismiss = { viewModel.onDeleteGameRejected() },
        )
    }
    errorDialog?.let { dialog ->
        ErrorDialog(
            message = dialog.message,
            onDismiss = { viewModel.onErrorDialogDismissed() },
        )
    }
}

@Composable
private fun TvGameInfoScreenContent(
    state: GameInfoScreenState,
    onInstallClick: () -> Unit,
    onRunClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    if (state.name.isBlank()) {
        return
    }

    val installFocus = remember { FocusRequester() }
    val runFocus = remember { FocusRequester() }
    val cancelFocus = remember { FocusRequester() }
    val cancelVisible = state.showCancelButton
    val installedVisible = state.state == GameState.INSTALLED && !state.showProgress
    val installVisible = state.state == GameState.NO_INSTALLED && !state.showProgress
    LaunchedEffect(cancelVisible) {
        if (cancelVisible) {
            cancelFocus.requestFocus()
        }
    }
    LaunchedEffect(installedVisible) {
        if (installedVisible) {
            runFocus.requestFocus()
        }
    }
    LaunchedEffect(installVisible) {
        if (installVisible) {
            installFocus.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(state.imageUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.walking_cat),
                error = painterResource(R.drawable.sleeping_cat),
                contentDescription = state.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Spacer(modifier = Modifier.height(24.dp))
            PropertiesBlock(state = state)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (state.showProgress) {
                ProgressBlock(
                    state = state,
                    onCancelClick = onCancelClick,
                    cancelFocus = cancelFocus,
                )
            } else {
                ButtonsBlock(
                    state = state,
                    onInstallClick = onInstallClick,
                    onRunClick = onRunClick,
                    onUpdateClick = onUpdateClick,
                    onDeleteClick = onDeleteClick,
                    installFocus = installFocus,
                    runFocus = runFocus,
                )
            }
            TvScrollableDescription(
                text = state.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun TvScrollableDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    if (text.isBlank()) {
        return
    }
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
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun PropertiesBlock(state: GameInfoScreenState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        GameProperty(
            icon = R.drawable.ic_account_circle_24dp,
            color = MaterialTheme.colorScheme.tertiary,
            text = state.author,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            GameProperty(
                icon = R.drawable.ic_file_compare_24dp,
                color = MaterialTheme.colorScheme.secondary,
                text = state.version,
            )
            GameProperty(
                icon = R.drawable.ic_math_compass_24dp,
                color = MaterialTheme.colorScheme.secondary,
                text = state.size,
            )
        }
    }
}

@Composable
private fun GameProperty(
    icon: Int,
    color: androidx.compose.ui.graphics.Color,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ButtonsBlock(
    state: GameInfoScreenState,
    onInstallClick: () -> Unit,
    onRunClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    installFocus: FocusRequester,
    runFocus: FocusRequester,
) {
    when (state.state) {
        GameState.NO_INSTALLED -> {
            Button(
                modifier = Modifier
                    .width(240.dp)
                    .focusRequester(installFocus),
                onClick = onInstallClick,
            ) {
                Text(
                    text = stringResource(R.string.game_activity_button_install),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        GameState.INSTALLED -> {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.isUpdateButtonShow) {
                    Button(
                        modifier = Modifier.width(240.dp),
                        onClick = onUpdateClick,
                    ) {
                        Text(
                            text = stringResource(R.string.game_activity_button_update),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Button(
                    modifier = Modifier
                        .width(240.dp)
                        .focusRequester(runFocus),
                    onClick = onRunClick,
                ) {
                    Text(
                        text = stringResource(R.string.game_activity_button_run),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    modifier = Modifier.width(240.dp),
                    onClick = onDeleteClick,
                ) {
                    Text(
                        text = stringResource(R.string.game_activity_button_uninstall),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        else -> Unit
    }
}

@Composable
private fun ProgressBlock(
    state: GameInfoScreenState,
    onCancelClick: () -> Unit,
    cancelFocus: FocusRequester,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = state.progressMessage, style = MaterialTheme.typography.bodyLarge)
        val animatedProgress by animateFloatAsState(
            targetValue = (state.progress as? ProgressType.WithValue)?.value ?: 0f,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        )
        if (state.showIndeterminateProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.showCancelButton) {
            Button(
                modifier = Modifier.focusRequester(cancelFocus),
                onClick = onCancelClick,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.game_activity_button_cancel_download))
                }
            }
        }
    }
}
