/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.presentation.models.GameInfoScreenState
import org.emunix.insteadlauncher.presentation.models.ProgressType
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameInfoScreen(
    state: GameInfoScreenState,
    onBackClick: () -> Unit,
    onInstallClick: () -> Unit,
    onRunClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFeedbackClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .safeDrawingPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(state.title)
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick.invoke() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (state.state == GameState.INSTALLED || state.siteUrl.isNotBlank()) {
                        ToolbarMenu(
                            state = state,
                            onDeleteClick = onDeleteClick,
                            onFeedbackClick = onFeedbackClick,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        if (state.name.isNotBlank()) {
            Box(Modifier.padding(innerPadding)) {
                GameInfoContent(
                    state = state,
                    onInstallClick = onInstallClick,
                    onRunClick = onRunClick,
                    onUpdateClick = onUpdateClick,
                )
            }
        }
    }
}

@Composable
private fun ToolbarMenu(
    state: GameInfoScreenState,
    onDeleteClick: () -> Unit,
    onFeedbackClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (state.state == GameState.INSTALLED) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null) },
                    text = { Text(stringResource(R.string.game_activity_button_uninstall)) },
                    onClick = {
                        expanded = false
                        onDeleteClick()
                    }
                )
            }
            if (state.siteUrl.isNotBlank()) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.Feedback, contentDescription = null) },
                    text = { Text(stringResource(R.string.game_activity_button_feedback)) },
                    onClick = {
                        expanded = false
                        onFeedbackClick()
                    }
                )
            }
        }
    }
}

@Composable
fun GameInfoContent(
    state: GameInfoScreenState,
    onInstallClick: () -> Unit,
    onRunClick: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameImage(state.imageUrl)
        PropertiesBlock(state)
        Spacer(Modifier.height(16.dp))
        if (state.showProgress) {
            ProgressBlock(state)
        } else {
            ButtonsBlock(
                state = state,
                onInstallClick = onInstallClick,
                onRunClick = onRunClick,
                onUpdateClick = onUpdateClick,
            )
        }
        Spacer(Modifier.height(16.dp))
        TextBlock(state.description)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun GameImage(
    url: String
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.walking_cat),
        error = painterResource(R.drawable.sleeping_cat),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth(),
    )
}

@Composable
private fun PropertiesBlock(
    state: GameInfoScreenState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameProperty(
            icon = R.drawable.ic_account_circle_24dp,
            color = MaterialTheme.colorScheme.tertiary,
            text = state.author,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameProperty(
                icon = R.drawable.ic_file_compare_24dp,
                color = MaterialTheme.colorScheme.primary,
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
    @DrawableRes
    icon: Int,
    color: Color,
    text: String,
) {
    Row {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = color,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text
        )
    }
}

@Composable
private fun ButtonsBlock(
    state: GameInfoScreenState,
    onInstallClick: () -> Unit,
    onRunClick: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    when (state.state) {
        GameState.NO_INSTALLED -> {
            Button(
                modifier = Modifier.defaultMinSize(minWidth = 120.dp),
                onClick = onInstallClick
            ) {
                Text(stringResource(R.string.game_activity_button_install))
            }
        }

        GameState.INSTALLED -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isUpdateButtonShow) {
                    FilledTonalButton(
                        modifier = Modifier.defaultMinSize(minWidth = 120.dp),
                        onClick = onUpdateClick
                    ) {
                        Text(stringResource(R.string.game_activity_button_update))
                    }
                }
                Button(
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp),
                    onClick = onRunClick
                ) {
                    Text(stringResource(R.string.game_activity_button_run))
                }
            }
        }

        else -> Unit
    }
}

@Composable
private fun ProgressBlock(
    state: GameInfoScreenState,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = (state.progress as? ProgressType.WithValue)?.value ?: 0f,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        )
        Text(text = state.progressMessage)
        if (state.progress is ProgressType.Indeterminate) {
            LinearProgressIndicator()
        } else {
            LinearProgressIndicator(progress = { animatedProgress })
        }
    }
}

@Composable
private fun TextBlock(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Preview(showBackground = true, widthDp = 400)
@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun GameInfoScreenPreview() {
    InsteadLauncherTheme {
        GameInfoScreen(
            state = GameInfoScreenState(
                name = "cat",
                title = "Возвращение квантового кота",
                author = "Пётр Косых",
                description = "Interactive fiction game about quantum mechanics and ... the cat :)\n" +
                        "\n" +
                        "Возвращение квантового кота\n" +
                        "\n" +
                        "Russian/English languages.\n" +
                        "\n" +
                        "За окном моей хижины снова белеет снег, а в камине также как и тогда потрескивают дрова… Третья зима. Прошло уже две зимы, но те события, о которых я хочу рассказать, встают перед моими глазами так, словно это было вчера…\n" +
                        "\n" +
                        "История о леснике хакере-дауншифтере, его войне со злом и квантовых парадоксах.\n" +
                        "\n" +
                        "– Я ПРОСТО ПРИШЕЛ ЗАБРАТЬ СВОЕГО КОТА…\n" +
                        "\n" +
                        "Первая игра для платформы STEAD, содержит около 70 сцен. Продолжение приключений главного героя этой игры (под другим именем) содержится в игре Куба.\n" +
                        "\n" +
                        "Первое место на КРИЛ-2009.\n" +
                        "\n" +
                        "*** The Returning of the Quantum Cat ***\n" +
                        "\n" +
                        "Outside my cabin the snow is white again. The wood crackles in the fireplace just like that day... It's the third winter already. Two winters have passed, but the events I want to tell about are in front of my eyes as if it was yesterday... ",
                version = "Версия: 1.6.1",
                size = "Размер: 5 Mb",
                imageUrl = "",
                siteUrl = "https://instead-games.ru/game.php?ID=107",
                state = GameState.IN_QUEUE_TO_INSTALL,
                isUpdateButtonShow = true,
                showProgress = true,
                progress = ProgressType.WithValue(0.4f),
                progressMessage = "Скачано 813 байт",
            ),
            onBackClick = {},
            onInstallClick = {},
            onRunClick = {},
            onUpdateClick = {},
            onDeleteClick = {},
            onFeedbackClick = {},
        )
    }
}