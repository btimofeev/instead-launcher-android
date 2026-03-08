/*
 * Copyright (c) 2025-2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.unpackresources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.ERROR
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.SUCCESS
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.UNPACKING
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@Composable
fun UnpackResourcesScreen(navigateToInstalledGamesScreen: () -> Unit) {
    val viewModel: UnpackResourcesViewModel = hiltViewModel()
    val state by viewModel.screenState.collectAsState()

    UnpackResourcesScreenContent(
        state = state,
        onSuccessUnpack = navigateToInstalledGamesScreen,
        onTryAgainClick = viewModel::tryAgainIsClicked
    )
}

@Composable
fun UnpackResourcesScreenContent(
    state: UnpackResourcesScreenState,
    onSuccessUnpack: () -> Unit,
    onTryAgainClick: () -> Unit,
) {
    Scaffold(modifier = Modifier.safeDrawingPadding()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (state) {
                SUCCESS -> onSuccessUnpack.invoke()
                UNPACKING -> UnpackResourcesLoadingScreen()
                ERROR -> UnpackResourcesErrorScreen(
                    onTryAgainClick = onTryAgainClick
                )
            }
        }
    }
}

@Composable
fun UnpackResourcesLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun UnpackResourcesErrorScreen(
    onTryAgainClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.unpack_resources_fragment_error_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onTryAgainClick,
        ) {
            Text(
                text = stringResource(R.string.unpack_resources_fragment_try_again_button),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 400)
fun UnpackResourcesLoadingScreenPreview() {
    InsteadLauncherTheme {
        UnpackResourcesLoadingScreen()
    }
}

@Composable
@Preview
fun UnpackResourcesErrorScreenPreview() {
    InsteadLauncherTheme {
        UnpackResourcesErrorScreen(
            onTryAgainClick = { }
        )
    }
}