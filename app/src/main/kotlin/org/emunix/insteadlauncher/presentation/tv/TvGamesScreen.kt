/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.SingletonImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.dialogs.ErrorDialog
import org.emunix.insteadlauncher.presentation.models.UpdateRepoState
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

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

    DisposableEffect(Unit) {
        onDispose { viewModel.onScreenDisposed() }
    }

    val pendingFocusItem = remember { viewModel.consumePendingFocusItem() }
    LaunchedEffect(pendingFocusItem, state.installedGames, state.catalogGames) {
        val focused = pendingFocusItem ?: return@LaunchedEffect
        val targetIndex = if (focused.key.isUiKey()) {
            HEADER_ITEM_INDEX
        } else {
            snapshotFlow { state.installedGames.isNotEmpty() || state.catalogGames.isNotEmpty() }
                .first { it }
            state.indexOfGameKey(focused.key) ?: focused.index
        }
        val totalItemsCount = snapshotFlow { gridState.layoutInfo.totalItemsCount }
            .first { it > 0 }
        val safeIndex = targetIndex.coerceIn(0, totalItemsCount - 1)
        if (gridState.layoutInfo.visibleItemsInfo.none { it.index == safeIndex }) {
            gridState.scrollToItem(safeIndex)
        }
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .first { visible -> visible.any { it.index == safeIndex } }
        val visible = gridState.layoutInfo.visibleItemsInfo
        val focusKey = if (focused.key.isUiKey() || state.indexOfGameKey(focused.key) != null) {
            focused.key
        } else {
            val target = visible.firstOrNull { it.index == safeIndex }
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
        onItemFocused = viewModel::onItemFocused,
        onGameClick = { key, name -> onGameClick(name) },
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
    onItemFocused: (key: String, index: Int) -> Unit,
    onGameClick: (key: String, name: String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
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
                    Button(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .focusRequester(focusRequesters.getOrPut(SEARCH_BUTTON_KEY) { FocusRequester() })
                            .onFocusChanged {
                                if (it.isFocused) onItemFocused(SEARCH_BUTTON_KEY, HEADER_ITEM_INDEX)
                            },
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_search))
                    }
                    Button(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .focusRequester(focusRequesters.getOrPut(SETTINGS_BUTTON_KEY) { FocusRequester() })
                            .onFocusChanged {
                                if (it.isFocused) onItemFocused(SETTINGS_BUTTON_KEY, HEADER_ITEM_INDEX)
                            },
                    ) {
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
                    onClick = { onGameClick(key, game.name) },
                    focusRequester = focusRequesters.getOrPut(key) { FocusRequester() },
                    onFocused = { onItemFocused(key, state.indexOfGameKey(key) ?: -1) },
                )
            }
        }

        if (state.catalogGames.isNotEmpty() || state.updateRepo != UpdateRepoState.HIDDEN) {
            val sectionTitle = when (state.updateRepo) {
                UpdateRepoState.UPDATING -> R.string.tv_games_section_updating
                UpdateRepoState.ERROR -> R.string.tv_games_section_catalog
                UpdateRepoState.HIDDEN -> R.string.tv_games_section_not_installed
            }
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "section") {
                Text(
                    text = stringResource(sectionTitle),
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
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "error_image") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.boy_and_cat),
                            contentDescription = null,
                            modifier = Modifier.size(280.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
            if (state.updateRepo == UpdateRepoState.HIDDEN) {
                items(state.catalogGames, key = { "catalog_${it.name}" }, contentType = { "game" }) { game ->
                    val key = "catalog_${game.name}"
                    TvGameCard(
                        item = game,
                        onClick = { onGameClick(key, game.name) },
                        focusRequester = focusRequesters.getOrPut(key) { FocusRequester() },
                        onFocused = { onItemFocused(key, state.indexOfGameKey(key) ?: -1) },
                    )
                }
            }
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
private const val SEARCH_BUTTON_KEY = "search_button"
private const val SETTINGS_BUTTON_KEY = "settings_button"
private const val HEADER_ITEM_INDEX = 0

private fun String.isUiKey() = this == SEARCH_BUTTON_KEY || this == SETTINGS_BUTTON_KEY

private val previewInstalledGames = listOf(
    TvGameItem(name = "quantum_cat", title = "Возвращение квантового кота", imageUrl = ""),
    TvGameItem(name = "alpha_centauri", title = "Полёт квантового кота на Альфа-Центавра", imageUrl = ""),
    TvGameItem(name = "old_castle", title = "Старый замок", imageUrl = ""),
)

private val previewCatalogGames = listOf(
    TvGameItem(name = "lighthouse", title = "Тайна старого маяка", imageUrl = ""),
    TvGameItem(name = "colony", title = "Колония", imageUrl = ""),
    TvGameItem(name = "moon", title = "Путешествие на Луну", imageUrl = ""),
)

@Composable
private fun TvGamesScreenPreview(state: TvGamesState) {
    InsteadLauncherTheme(darkTheme = true, dynamicColor = false) {
        TvTheme {
            TvGamesScreenContent(
                state = state,
                gridState = rememberLazyGridState(),
                focusRequesters = remember { mutableMapOf() },
                onItemFocused = { _, _ -> },
                onGameClick = { _, _ -> },
                onSearchClick = {},
                onSettingsClick = {},
                onRetryClick = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TvGamesScreenLoadedPreview() {
    TvGamesScreenPreview(
        TvGamesState(
            installedGames = previewInstalledGames,
            catalogGames = previewCatalogGames,
        ),
    )
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540, uiMode = UI_MODE_NIGHT_YES, name = "Loading")
@Composable
private fun TvGamesScreenLoadingPreview() {
    TvGamesScreenPreview(
        TvGamesState(
            installedGames = previewInstalledGames,
            updateRepo = UpdateRepoState.UPDATING,
        ),
    )
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540, uiMode = UI_MODE_NIGHT_YES, name = "Loading empty")
@Composable
private fun TvGamesScreenLoadingEmptyPreview() {
    TvGamesScreenPreview(
        TvGamesState(updateRepo = UpdateRepoState.UPDATING),
    )
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540, uiMode = UI_MODE_NIGHT_YES, name = "Error")
@Composable
private fun TvGamesScreenErrorPreview() {
    TvGamesScreenPreview(
        TvGamesState(
            installedGames = previewInstalledGames,
            updateRepo = UpdateRepoState.ERROR,
        ),
    )
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540, uiMode = UI_MODE_NIGHT_YES, name = "Error empty")
@Composable
private fun TvGamesScreenErrorEmptyPreview() {
    TvGamesScreenPreview(
        TvGamesState(updateRepo = UpdateRepoState.ERROR),
    )
}
