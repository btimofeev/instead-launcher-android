package org.emunix.insteadlauncher.presentation.repository

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.ErrorDialogModel
import org.emunix.insteadlauncher.presentation.models.RepoGame
import org.emunix.insteadlauncher.presentation.models.RepoScreenState
import org.emunix.insteadlauncher.presentation.models.UpdateRepoState
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryScreen(
    state: RepoScreenState,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onUpdateRepositoryClick: () -> Unit,
    onInstallFromZipClick: () -> Unit,
    onGameClick: (gameName: String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.repository_activity_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    ToolbarMenu(
                        onSearchClick = onSearchClick,
                        onUpdateRepositoryClick = onUpdateRepositoryClick,
                        onInstallFromZipClick = onInstallFromZipClick,
                    )
                }
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.updateRepo == UpdateRepoState.UPDATING,
            onRefresh = onUpdateRepositoryClick,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column {
                if (state.installGameProgress) {
                    InstallGameProgressBar()
                }
                when (state.updateRepo) {
                    UpdateRepoState.HIDDEN -> {
                        if (state.games.isNotEmpty()) {
                            GamesScreen(
                                games = state.games,
                                onGameClick = onGameClick,
                            )
                        }
                    }

                    UpdateRepoState.UPDATING -> Unit
                    UpdateRepoState.ERROR -> {
                        ErrorUpdateRepositoryView(onUpdateRepositoryClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.InstallGameProgressBar() {
    Column(
        modifier = Modifier.Companion
            .align(Alignment.CenterHorizontally)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(percent = 30)
            )
            .padding(18.dp)
    ) {
        Text(text = stringResource(R.string.notification_install_game))
        LinearProgressIndicator()
    }
}

@Composable
private fun ErrorUpdateRepositoryView(onUpdateRepositoryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.repository_unable_to_load),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onUpdateRepositoryClick) {
            Text(text = stringResource(R.string.repository_try_again))
        }
    }
}

@Composable
private fun ToolbarMenu(
    onSearchClick: () -> Unit,
    onUpdateRepositoryClick: () -> Unit,
    onInstallFromZipClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row {
            IconButton(onClick = { onSearchClick() }) {
                Icon(Icons.Default.Search, contentDescription = null)
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_update_repo)) },
                onClick = {
                    expanded = false
                    onUpdateRepositoryClick()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_install_local_game)) },
                onClick = {
                    expanded = false
                    onInstallFromZipClick()
                }
            )
        }
    }
}

@Composable
private fun GamesScreen(
    games: List<RepoGame>,
    onGameClick: (gameName: String) -> Unit,
) {
    LazyColumn {
        items(games, key = { it.name }) { item ->
            GameItem(
                modifier = Modifier.animateItem(),
                item = item,
                onGameClick = onGameClick,
            )
        }
    }
}

@Composable
private fun GameItem(
    modifier: Modifier,
    item: RepoGame,
    onGameClick: (gameName: String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGameClick(item.name) },
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
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (item.isHasNewVersion) {
                    Image(
                        painter = painterResource(R.drawable.ic_new_red_16dp),
                        modifier = Modifier.size(16.dp),
                        contentDescription = stringResource(R.string.repository_badge_content_description),
                    )
                }
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = item.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun ErrorDialog(
    model: ErrorDialogModel,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) },
        title = { Text(text = model.title) },
        text = { Text(text = model.message) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dialog_error_close_button))
            }
        },
    )
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
        RepositoryScreen(
            state = RepoScreenState(
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
                updateRepo = UpdateRepoState.HIDDEN,
                installGameProgress = true,
            ),
            onBackClick = {},
            onSearchClick = {},
            onUpdateRepositoryClick = {},
            onInstallFromZipClick = {},
            onGameClick = {},
        )
    }
}