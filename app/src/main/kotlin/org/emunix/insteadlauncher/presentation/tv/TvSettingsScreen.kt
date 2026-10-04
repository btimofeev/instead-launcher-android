/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.dialogs.CustomDialog
import org.emunix.insteadlauncher.presentation.models.SettingsItem
import org.emunix.insteadlauncher.presentation.settings.SettingsViewModel

private const val TV_ABOUT_ID = "tv_about"

private val tvExcludedSettingsIds = setOf(
    PreferencesProvider.PREF_CURSOR_KEY,
    PreferencesProvider.PREF_KEYBOARD_BUTTON_KEY,
    PreferencesProvider.PREF_BACK_BUTTON_KEY,
    PreferencesProvider.PREF_APP_THEME_KEY,
    PreferencesProvider.PREF_DYNAMIC_COLORS_KEY,
    PreferencesProvider.PREF_UPDATE_REPO_BACKGROUND_KEY,
    PreferencesProvider.PREF_UPDATE_REPO_STARTUP_KEY,
    PreferencesProvider.PREF_HIRES_KEY,
)

@Composable
fun TvSettingsScreen(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val items by viewModel.items.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    val aboutItem = SettingsItem.Element(
        id = TV_ABOUT_ID,
        icon = R.drawable.ic_info_24dp,
        title = stringResource(R.string.about_activity_title),
        onClick = onAboutClick,
    )

    val tvItems = remember(items) { items.filterForTv() + aboutItem }
    TvSettingsScreenContent(items = tvItems)
    CustomDialog(
        model = showDialog,
        onCloseDialog = viewModel::onDialogClosed,
    )
}

private fun List<SettingsItem>.filterForTv(): List<SettingsItem> {
    val filtered = filterNot { it is SettingsItem.Element && it.id in tvExcludedSettingsIds }
    val segments = mutableListOf<Pair<SettingsItem.Category?, List<SettingsItem>>>()
    var category: SettingsItem.Category? = null
    var body = mutableListOf<SettingsItem>()
    filtered.forEach { item ->
        if (item is SettingsItem.Category) {
            segments.add(category to body)
            category = item
            body = mutableListOf()
        } else {
            body.add(item)
        }
    }
    segments.add(category to body)

    return segments
        .filter { (_, segmentBody) -> segmentBody.any { it !is SettingsItem.Divider } }
        .flatMap { (segmentCategory, segmentBody) ->
            val trimmedBody =
                if (segmentBody.lastOrNull() is SettingsItem.Divider) {
                    segmentBody.dropLast(1)
                } else {
                    segmentBody
                }
            val body = trimmedBody
            listOfNotNull(segmentCategory) + body
        }
}

@Composable
private fun TvSettingsScreenContent(items: List<SettingsItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
    ) {
        items(
            count = items.size,
            contentType = { index -> items[index]::class.simpleName },
        ) { index ->
            when (val item = items[index]) {
                is SettingsItem.Category -> {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 32.dp, bottom = 12.dp),
                    )
                }

                is SettingsItem.Divider -> {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }

                is SettingsItem.Element -> {
                    TvSettingsRow(item = item)
                }

                is SettingsItem.Slider -> {
                    TvTextSizeRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun TvSettingsRow(item: SettingsItem.Element) {
    if (item.isEnabled) {
        Card(
            onClick = item.onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = tvCardColors(),
            border = tvCardFocusBorder(),
        ) {
            TvSettingsRowContent(item = item)
        }
    } else {
        TvSettingsRowContent(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .alpha(0.5f),
        )
    }
}

@Composable
private fun TvSettingsRowContent(
    item: SettingsItem.Element,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (item.icon != null) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (item.description != null) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val switchState = item.switchState
        if (switchState != null) {
            Text(
                text = stringResource(
                    if (switchState) R.string.tv_settings_switch_on
                    else R.string.tv_settings_switch_off
                ),
                style = MaterialTheme.typography.titleMedium,
                color = if (switchState) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun TvTextSizeRow(item: SettingsItem.Slider) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (item.icon != null) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Button(
            enabled = item.isEnabled && item.value > item.valueRange.first,
            onClick = {
                item.onValueChange(
                    (item.value - PreferencesProvider.INSTEAD_TEXT_SIZE_STEP)
                        .coerceIn(item.valueRange)
                )
            },
        ) {
            Text(text = "−")
        }
        Text(
            text = stringResource(R.string.slider_value_percent, item.value),
            style = MaterialTheme.typography.titleMedium,
        )
        Button(
            enabled = item.isEnabled && item.value < item.valueRange.last,
            onClick = {
                item.onValueChange(
                    (item.value + PreferencesProvider.INSTEAD_TEXT_SIZE_STEP)
                        .coerceIn(item.valueRange)
                )
            },
        ) {
            Text(text = "+")
        }
    }
    Spacer(modifier = Modifier.size(8.dp))
}
