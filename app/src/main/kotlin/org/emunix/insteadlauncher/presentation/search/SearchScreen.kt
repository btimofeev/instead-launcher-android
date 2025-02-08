/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.search

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.RepoGame
import org.emunix.insteadlauncher.presentation.models.SearchScreenState
import org.emunix.insteadlauncher.presentation.repository.GameItem
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@Composable
fun SearchScreen(
    state: SearchScreenState,
    onSearchQueryChange: (query: String) -> Unit,
    onBackClick: () -> Unit,
    onGameClick: (gameName: String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = "request_focus") {
        focusRequester.requestFocus()
    }
    Surface(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column {
            Row(
                modifier = Modifier.height(72.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBackClick() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                var value by rememberSaveable { mutableStateOf("") }
                TextField(
                    value = value,
                    onValueChange = { newValue ->
                        value = newValue
                        onSearchQueryChange(newValue)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) }
                )

                IconButton(
                    onClick = {
                        value = ""
                        onSearchQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            when (state) {
                is SearchScreenState.Empty -> Unit

                is SearchScreenState.Result -> {
                    GamesScreen(
                        games = state.games,
                        onGameClick = onGameClick,
                    )
                }

                is SearchScreenState.NothingFound -> {
                    Column(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeContent)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(54.dp),
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.repository_nothing_found),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GamesScreen(
    games: List<RepoGame>,
    onGameClick: (gameName: String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(games, key = { it.name }) { item ->
            GameItem(
                modifier = Modifier.animateItem(),
                item = item,
                onGameClick = onGameClick,
            )
        }
    }
}

@Preview(
    showBackground = true, widthDp = 400, uiMode = UI_MODE_NIGHT_YES, name = "Dark"
)
@Preview(showBackground = true, widthDp = 400)
@Composable
private fun InstalledGamesScreenPreview() {
    InsteadLauncherTheme {
        SearchScreen(
            //state = SearchScreenState.NothingFound,
            state = SearchScreenState.Result(
                games = listOf(
                    RepoGame(
                        name = "cat",
                        title = "Возвращение квантового кота",
                        imageUrl = "",
                        description = "Игра про кота, которого похитили и хозяина, который его спасал",
                        isHasNewVersion = true,
                    ),
                    RepoGame(
                        name = "cat2",
                        title = "Возвращение квантового кота 3",
                        imageUrl = "",
                        description = "Игра про кота, которого в который раз похитили и хозяина, который его спасал",
                        isHasNewVersion = false,
                    )
                ),
            ),
            onSearchQueryChange = {},
            onBackClick = {},
            onGameClick = {},
        )
    }
}