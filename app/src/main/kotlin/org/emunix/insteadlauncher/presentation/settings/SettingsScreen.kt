/*
 * Copyright (c) 2025-2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.dialogs.CustomDialog
import org.emunix.insteadlauncher.presentation.models.SettingsItem
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme
import org.emunix.insteadlauncher.utils.ThemeSwitcherDelegate

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val items by viewModel.items.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    LaunchedEffect(Unit) {
        viewModel.changeAppTheme.collect { themeName ->
            ThemeSwitcherDelegate().applyTheme(themeName)
        }
    }

    SettingsScreenContent(
        items = items,
        onBackClick = onBackClick,
    )
    showDialog?.let { dialogModel ->
        CustomDialog(
            model = dialogModel,
            onCloseDialog = viewModel::onDialogClosed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    items: List<SettingsItem>,
    onBackClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_activity_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_24dp),
                            contentDescription = null
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 800.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items.forEach { item ->
                    when (item) {
                        is SettingsItem.Category -> Category(item)
                        is SettingsItem.Element -> Element(item)
                        is SettingsItem.Divider -> Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 1.dp,
    )
}

@Composable
private fun Category(item: SettingsItem.Category) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                end = 16.dp,
                start = 64.dp,
            ),
        text = item.title,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun Element(item: SettingsItem.Element) {
    CompositionLocalProvider(
        value = LocalContentColor provides
                if (item.isEnabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = item.isEnabled,
                    onClick = item.onClick
                )
                .padding(top = 12.dp, bottom = 12.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.icon != null) {
                Icon(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 24.dp),
                    painter = painterResource(item.icon),
                    contentDescription = null,
                )
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (item.description != null) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                            .copy(alpha = LocalContentColor.current.alpha)
                    )
                }
            }
            if (item.switchState != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = item.switchState,
                    onCheckedChange = null,
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
@PreviewScreenSizes
fun UnpackResourcesErrorScreenContentPreview() {
    InsteadLauncherTheme {
        SettingsScreenContent(
            items = listOf(
                SettingsItem.Category(title = "Game settings"),
                SettingsItem.Element(
                    id = "1",
                    icon = R.drawable.ic_headphones_24dp,
                    title = "Enable music",
                    switchState = true,
                    onClick = { },
                ),
                SettingsItem.Divider,
                SettingsItem.Category(title = "Other settings"),
                SettingsItem.Element(
                    id = "2",
                    title = "Select theme and lonl long extra long text",
                    description = "tap to open menu",
                    switchState = null,
                    onClick = { },
                ),
                SettingsItem.Element(
                    id = "3",
                    icon = R.drawable.ic_image_album_24dp,
                    title = "Disabled element",
                    description = "tap to open menu",
                    switchState = null,
                    isEnabled = false,
                    onClick = { },
                ),
                SettingsItem.Element(
                    id = "4",
                    icon = R.drawable.ic_cursor_24dp,
                    title = "Show cursor",
                    description = "click me if you want show cursor in the game",
                    switchState = false,
                    onClick = { },
                ),
            ),
            onBackClick = { }
        )
    }
}