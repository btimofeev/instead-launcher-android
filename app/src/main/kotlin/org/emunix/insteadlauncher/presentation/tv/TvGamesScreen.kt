/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.SingletonImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.dialogs.ErrorDialog
import org.emunix.insteadlauncher.presentation.models.UpdateRepoState

@Composable
fun TvGamesScreen(
    zipUri: Uri?,
    onGameClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val viewModel: TvGamesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    LaunchedEffect(zipUri) {
        if (zipUri != null) {
            viewModel.installGameFromZip(zipUri)
        }
    }

    val context = LocalContext.current
    LaunchedEffect(state.installedGames, state.catalogGames) {
        val imageLoader = SingletonImageLoader.get(context)
        (state.installedGames + state.catalogGames).forEach { game ->
            if (game.imageUrl.isNotBlank()) {
                imageLoader.enqueue(gameImageRequest(context, game.imageUrl))
            }
            delay(IMAGE_PREFETCH_INTERVAL_MS)
        }
    }

    val restoreKey = viewModel.lastOpenedGameKey
    LaunchedEffect(restoreKey) {
        if (restoreKey == null) {
            return@LaunchedEffect
        }
        snapshotFlow { state.installedGames.isNotEmpty() || state.catalogGames.isNotEmpty() }
            .first { it }
        val index = state.indexOfGameKey(restoreKey)
        val targetIndex = if (index != null) {
            index
        } else {
            val oldIndex = viewModel.lastOpenedGameIndex
            val total = gridState.layoutInfo.totalItemsCount
            if (oldIndex < 0 || total <= 0) {
                return@LaunchedEffect
            }
            oldIndex.coerceIn(0, total - 1)
        }
        gridState.scrollToItem(targetIndex)
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .first { visible -> visible.any { it.index == targetIndex } }
        val visible = gridState.layoutInfo.visibleItemsInfo
        val focusKey = if (index != null) {
            restoreKey
        } else {
            val target = visible.firstOrNull { it.index == targetIndex }
            val candidates = buildList {
                if (target != null) {
                    visible.filter { it.row == target.row }
                        .sortedBy { it.index }
                        .forEach { add(it.key) }
                }
                visible.sortedBy { it.index }.forEach { add(it.key) }
            }
            candidates.filterIsInstance<String>().firstOrNull { focusRequesters.containsKey(it) }
        }
        focusKey?.let { focusRequesters[it]?.requestFocus() }
    }

    TvGamesScreenContent(
        state = state,
        gridState = gridState,
        focusRequesters = focusRequesters,
        onGameClick = { key, index, name ->
            viewModel.rememberOpenedGame(key, index)
            onGameClick(name)
        },
        onSearchClick = onSearchClick,
        onSettingsClick = onSettingsClick,
        onRetryClick = { viewModel.updateRepository() },
    )
    state.errorMessage?.let { message ->
        ErrorDialog(
            message = message,
            onDismiss = { viewModel.onErrorDismissed() },
        )
    }
}

@Composable
private fun TvGamesScreenContent(
    state: TvGamesState,
    gridState: LazyGridState,
    focusRequesters: MutableMap<String, FocusRequester>,
    onGameClick: (key: String, index: Int, name: String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val showEmptyState = state.installedGames.isEmpty() &&
            state.catalogGames.isEmpty() &&
            state.updateRepo != UpdateRepoState.UPDATING

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 260.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.installed_games_screen_title),
                    style = MaterialTheme.typography.displaySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onSearchClick) {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_search))
                    }
                    Button(onClick = onSettingsClick) {
                        Icon(Icons.Rounded.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_settings))
                    }
                }
            }
        }

        if (state.installedGames.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "section") {
                Text(
                    text = stringResource(R.string.tv_games_section_installed),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            items(state.installedGames, key = { "installed_${it.name}" }, contentType = { "game" }) { game ->
                val key = "installed_${game.name}"
                TvGameCard(
                    item = game,
                    onClick = { onGameClick(key, state.indexOfGameKey(key) ?: -1, game.name) },
                    focusRequester = focusRequesters.getOrPut(key) { FocusRequester() },
                )
            }
        }

        if (state.catalogGames.isNotEmpty() || state.updateRepo != UpdateRepoState.HIDDEN) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "section") {
                Text(
                    text = stringResource(R.string.tv_games_section_not_installed),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            if (state.updateRepo == UpdateRepoState.UPDATING) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "progress") {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            if (state.updateRepo == UpdateRepoState.ERROR) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "error") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.repository_unable_to_load),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = onRetryClick) {
                            Text(text = stringResource(R.string.repository_try_again))
                        }
                    }
                }
            }
            items(state.catalogGames, key = { "catalog_${it.name}" }, contentType = { "game" }) { game ->
                val key = "catalog_${game.name}"
                TvGameCard(
                    item = game,
                    onClick = { onGameClick(key, state.indexOfGameKey(key) ?: -1, game.name) },
                    focusRequester = focusRequesters.getOrPut(key) { FocusRequester() },
                )
            }
        }

        if (showEmptyState) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                TvGamesEmptyState(onRetryClick = onRetryClick)
            }
        }
    }
}

@Composable
private fun TvGamesEmptyState(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.boy_and_cat),
            contentDescription = null,
            modifier = Modifier.size(280.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.tv_games_empty),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.action_update_repo))
        }
    }
}

private fun TvGamesState.indexOfGameKey(key: String): Int? {
    var index = 1
    if (installedGames.isNotEmpty()) {
        index++
        installedGames.forEachIndexed { i, game ->
            if ("installed_${game.name}" == key) {
                return index + i
            }
        }
        index += installedGames.size
    }
    if (catalogGames.isNotEmpty() || updateRepo != UpdateRepoState.HIDDEN) {
        index++
        if (updateRepo == UpdateRepoState.UPDATING) {
            index++
        }
        if (updateRepo == UpdateRepoState.ERROR) {
            index++
        }
        catalogGames.forEachIndexed { i, game ->
            if ("catalog_${game.name}" == key) {
                return index + i
            }
        }
    }
    return null
}

private const val IMAGE_PREFETCH_INTERVAL_MS = 120L
