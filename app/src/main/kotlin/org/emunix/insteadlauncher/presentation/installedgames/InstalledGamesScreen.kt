/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.InstalledGame
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme
import org.emunix.insteadlauncher.presentation.theme.neuchaFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledGamesScreen(
    games: List<InstalledGame>?,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onAddGameClick: () -> Unit,
    gameActions: GameActions,
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.installed_games_screen_title),
                    )
                },
                actions = {
                    ToolbarMenu(
                        onSettingsClick = onSettingsClick,
                        onAboutClick = onAboutClick,
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGameClick) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.installed_games_screen_fab_content_description)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                games == null -> Unit
                games.isEmpty() -> EmptyGamesScreen()
                else -> GamesScreen(games, gameActions)
            }
        }
    }
}

@Composable
private fun ToolbarMenu(
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_settings)) },
                onClick = {
                    expanded = false
                    onSettingsClick()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_about)) },
                onClick = {
                    expanded = false
                    onAboutClick()
                }
            )
        }
    }
}

@Composable
private fun EmptyGamesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.boy_and_cat),
            contentDescription = null,
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.installed_games_empty_text),
            fontFamily = neuchaFontFamily,
            fontSize = 24.sp,
            lineHeight = 1.5.em,
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun GamesScreen(
    games: List<InstalledGame>,
    gameActions: GameActions,
) {
    LazyColumn {
        items(games, key = { it.name }) { item ->
            GameItem(
                modifier = Modifier.animateItem(),
                item = item,
                gameActions = gameActions
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameItem(
    modifier: Modifier,
    item: InstalledGame,
    gameActions: GameActions,
) {
    var contextMenu by rememberSaveable { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { gameActions.onPlayClick(item.name) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    contextMenu = item.name
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.walking_cat),
            error = painterResource(R.drawable.sleeping_cat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(100.dp)
                .height(56.dp),
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = item.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (contextMenu != null) {
            GameActionsSheet(
                gameName = item.name,
                onDismissSheet = { contextMenu = null },
                gameActions = gameActions,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameActionsSheet(
    gameName: String,
    onDismissSheet: () -> Unit,
    gameActions: GameActions,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissSheet
    ) {
        ListItem(
            modifier = Modifier.clickable {
                onDismissSheet()
                gameActions.onPlayClick(gameName)
            },
            headlineContent = { Text(stringResource(R.string.installed_games_context_menu_play)) },
            leadingContent = { Icon(Icons.Default.PlayArrow, null) }
        )
        ListItem(
            modifier = Modifier.clickable {
                onDismissSheet()
                gameActions.onPlayFromBeginningClick(gameName)
            },
            headlineContent = { Text(stringResource(R.string.installed_games_context_menu_play_from_beginning)) },
            leadingContent = { Icon(Icons.Default.Replay, null) }
        )
        ListItem(
            modifier = Modifier.clickable {
                onDismissSheet()
                gameActions.onDeleteClick(gameName)
            },
            headlineContent = { Text(stringResource(R.string.installed_games_context_menu_delete)) },
            leadingContent = { Icon(Icons.Default.Delete, null) }
        )
        ListItem(
            modifier = Modifier.clickable {
                onDismissSheet()
                gameActions.onAboutClick(gameName)
            },
            headlineContent = { Text(stringResource(R.string.installed_games_context_menu_about)) },
            leadingContent = { Icon(Icons.Default.Info, null) }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Preview(showBackground = true, widthDp = 400)
@Composable
private fun InstalledGamesScreenPreview() {
    InsteadLauncherTheme {
        InstalledGamesScreen(
            games = listOf(
                InstalledGame(
                    name = "cat",
                    title = "Возвращение квантового кота",
                    imageUrl = "",
                ),
                InstalledGame(
                    name = "cat2",
                    title = "Полёт квантового кота на Альфа-Центавра",
                    imageUrl = "",
                ),
            ),
            onSettingsClick = {},
            onAboutClick = {},
            onAddGameClick = {},
            gameActions = GameActions(
                onPlayClick = {},
                onPlayFromBeginningClick = {},
                onDeleteClick = {},
                onAboutClick = {},
            ),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Preview(showBackground = true, widthDp = 400)
@Composable
private fun InstalledGamesScreenEmptyPreview() {
    InsteadLauncherTheme {
        InstalledGamesScreen(
            games = emptyList(),
            onSettingsClick = {},
            onAboutClick = {},
            onAddGameClick = {},
            gameActions = GameActions(
                onPlayClick = {},
                onPlayFromBeginningClick = {},
                onDeleteClick = {},
                onAboutClick = {},
            ),
        )
    }
}