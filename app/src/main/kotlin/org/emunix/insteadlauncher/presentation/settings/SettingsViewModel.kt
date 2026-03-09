/*
 * Copyright (c) 2025-2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_APP_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_BACK_BUTTON_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_CURSOR_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_DEFAULT_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_DYNAMIC_COLORS_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_ENABLE_GAME_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_GL_HACK_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_HIRES_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_KEYBOARD_BUTTON_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_MUSIC_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_REPOSITORY_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_SANDBOX_ENABLED_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_SANDBOX_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_TEXT_SIZE_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_UPDATE_REPO_BACKGROUND_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_UPDATE_REPO_STARTUP_KEY
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.presentation.PlatformInfo
import org.emunix.insteadlauncher.presentation.models.CustomDialogModel
import org.emunix.insteadlauncher.presentation.models.RadioButtonModel
import org.emunix.insteadlauncher.presentation.models.SettingsItem
import org.emunix.insteadlauncher.presentation.models.SettingsItem.Category
import org.emunix.insteadlauncher.presentation.models.SettingsItem.Divider
import org.emunix.insteadlauncher.presentation.models.SettingsItem.Element
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesProvider: PreferencesProvider,
    private val fileSystemRepository: FileSystemRepository,
    private val startUpdateRepositoryWorkUseCase: StartUpdateRepositoryWorkUseCase,
    private val stopUpdateRepositoryWorkUseCase: StopUpdateRepositoryWorkUseCase,
    private val resourceProvider: ResourceProvider,
    private val platformInfo: PlatformInfo,
): ViewModel() {

    val items get() = _items.asStateFlow()
    val showDialog get() = _showDialog.asStateFlow()
    val changeAppTheme get() = _changeAppTheme.asSharedFlow()

    private val _items = MutableStateFlow<List<SettingsItem>>(emptyList())
    private val _showDialog = MutableStateFlow<CustomDialogModel?>(null)
    private val _changeAppTheme = MutableSharedFlow<String>()

    private val keyboardPositions: Map<String, String> by lazy {
        val keys = resourceProvider.getStringArray(R.array.prefs_keyboard_button_values)
        val names = resourceProvider.getStringArray(R.array.prefs_keyboard_button_entries)
        buildKeyNameMap(keys = keys, names = names)
    }

    private val backButtonBehavior: Map<String, String> by lazy {
        val keys = resourceProvider.getStringArray(R.array.prefs_back_button_values)
        val names = resourceProvider.getStringArray(R.array.prefs_back_button_entries)
        buildKeyNameMap(keys = keys, names = names)
    }

    private val appThemes: Map<String, String> by lazy {
        val keys = resourceProvider.getStringArray(R.array.app_theme_values)
        val names = resourceProvider.getStringArray(R.array.app_theme_names)
        buildKeyNameMap(keys = keys, names = names)
    }

    private var insteadThemes: List<String> = emptyList()

    fun init() {
        loadSettings()
    }

    fun onDialogClosed() {
        _showDialog.value = null
    }

    private fun loadSettings() = viewModelScope.launch(Dispatchers.IO) {
        insteadThemes = fileSystemRepository.getInstalledThemeNames()
        val defaultInsteadTheme = preferencesProvider.defaultInsteadTheme
        val defaultKeyboardButtonName = keyboardPositions.getOrDefault(preferencesProvider.keyboardButtonPosition, "")
        val defaultBackButtonName = backButtonBehavior.getOrDefault(preferencesProvider.backButton, "")
        val defaultAppThemeName = appThemes.getOrDefault(preferencesProvider.appTheme, "")
        _items.value = listOfNotNull(
            Category(
                title = resourceProvider.getString(R.string.prefs_category_game_settings)
            ),
            Element(
                id = PREF_MUSIC_KEY,
                icon = R.drawable.ic_headphones_24dp,
                title = resourceProvider.getString(R.string.prefs_music_title),
                switchState = preferencesProvider.isMusicEnabled,
                onClick = {
                    val newState = !preferencesProvider.isMusicEnabled
                    preferencesProvider.isMusicEnabled = newState
                    updateSwitchState(id = PREF_MUSIC_KEY, switchState = newState)
                }
            ),
            Element(
                id = PREF_CURSOR_KEY,
                icon = R.drawable.ic_cursor_24dp,
                title = resourceProvider.getString(R.string.prefs_cursor_title),
                switchState = preferencesProvider.isCursorEnabled,
                onClick = {
                    val newState = !preferencesProvider.isCursorEnabled
                    preferencesProvider.isCursorEnabled = newState
                    updateSwitchState(id = PREF_CURSOR_KEY, switchState = newState)
                }
            ),
            Element(
                id = PREF_ENABLE_GAME_THEME_KEY,
                icon = R.drawable.ic_image_24dp,
                title = resourceProvider.getString(R.string.prefs_enable_game_theme_title),
                description = resourceProvider.getString(R.string.prefs_enable_game_theme_summary),
                switchState = preferencesProvider.isOwnGameThemeEnabled,
                onClick = {
                    val newState = !preferencesProvider.isOwnGameThemeEnabled
                    preferencesProvider.isOwnGameThemeEnabled = newState
                    updateSwitchState(id = PREF_ENABLE_GAME_THEME_KEY, switchState = newState)
                    updateEnabledState(id = PREF_DEFAULT_THEME_KEY, enabled = !newState)
                }
            ),
            Element(
                id = PREF_DEFAULT_THEME_KEY,
                icon = R.drawable.ic_image_album_24dp,
                title = resourceProvider.getString(R.string.prefs_default_theme_title),
                description = defaultInsteadTheme,
                isEnabled = !preferencesProvider.isOwnGameThemeEnabled,
                onClick = ::showInsteadThemesSelectionDialog,
            ),
            Element(
                id = PREF_HIRES_KEY,
                icon = R.drawable.ic_resize_24dp,
                title = resourceProvider.getString(R.string.prefs_hires_title),
                description = resourceProvider.getString(R.string.prefs_hires_summary),
                switchState = preferencesProvider.isHiresEnabled,
                onClick = {
                    val newState = !preferencesProvider.isHiresEnabled
                    preferencesProvider.isHiresEnabled = newState
                    updateSwitchState(id = PREF_HIRES_KEY, switchState = newState)
                }
            ),
            Element(
                id = PREF_TEXT_SIZE_KEY,
                icon = R.drawable.ic_format_size_24dp,
                title = resourceProvider.getString(R.string.prefs_text_size_title),
                description = resourceProvider.getString(R.string.prefs_text_size_summary),
                onClick = ::showTextSizeInputDialog
            ),
            Element(
                id = PREF_KEYBOARD_BUTTON_KEY,
                icon = R.drawable.ic_keyboard_outline_24dp,
                title = resourceProvider.getString(R.string.prefs_keyboard_button_title),
                description = defaultKeyboardButtonName,
                onClick = ::showKeyboardButtonPositionSelectionDialog
            ),
            Element(
                id = PREF_BACK_BUTTON_KEY,
                icon = R.drawable.ic_keyboard_backspace_24dp,
                title = resourceProvider.getString(R.string.prefs_back_button_title),
                description = defaultBackButtonName,
                onClick = ::showBackButtonBehaviorSelectionDialog
            ),
            Element(
                id = PREF_GL_HACK_KEY,
                icon = R.drawable.ic_image_broken_24dp,
                title = resourceProvider.getString(R.string.prefs_gl_hack_title),
                description = resourceProvider.getString(R.string.prefs_gl_hack_summary),
                switchState = preferencesProvider.isGLHackEnabled,
                onClick = {
                    val newState = !preferencesProvider.isGLHackEnabled
                    preferencesProvider.isGLHackEnabled = newState
                    updateSwitchState(id = PREF_GL_HACK_KEY, switchState = newState)
                }
            ),
            Divider,
            Category(
                title = resourceProvider.getString(R.string.prefs_category_repositories)
            ),
            Element(
                id = PREF_REPOSITORY_KEY,
                icon = R.drawable.ic_web_24dp,
                title = resourceProvider.getString(R.string.prefs_repository_title),
                description = resourceProvider.getString(R.string.prefs_repository_summary),
                onClick = ::showRepoUrlInputDialog
            ),
            Element(
                id = PREF_SANDBOX_ENABLED_KEY,
                icon = R.drawable.ic_sandbox_24dp,
                title = resourceProvider.getString(R.string.prefs_sandbox),
                description = resourceProvider.getString(R.string.prefs_sandbox_summary),
                switchState = preferencesProvider.isSandboxEnabled,
                onClick = {
                    val newState = !preferencesProvider.isSandboxEnabled
                    preferencesProvider.isSandboxEnabled = newState
                    updateSwitchState(id = PREF_SANDBOX_ENABLED_KEY, switchState = newState)
                    updateEnabledState(id = PREF_SANDBOX_KEY, enabled = newState)
                }
            ),
            Element(
                id = PREF_SANDBOX_KEY,
                title = resourceProvider.getString(R.string.prefs_sandbox),
                description = resourceProvider.getString(R.string.prefs_repository_summary),
                isEnabled = preferencesProvider.isSandboxEnabled,
                onClick = ::showSandboxUrlInputDialog,
            ),
            Element(
                id = PREF_UPDATE_REPO_BACKGROUND_KEY,
                icon = R.drawable.ic_sync_24dp,
                title = resourceProvider.getString(R.string.pref_update_repo_background),
                description = resourceProvider.getString(R.string.pref_update_repo_background_summary),
                switchState = preferencesProvider.updateRepoInBackground,
                onClick = {
                    val newState = !preferencesProvider.updateRepoInBackground
                    preferencesProvider.updateRepoInBackground = newState
                    updateSwitchState(id = PREF_UPDATE_REPO_BACKGROUND_KEY, switchState = newState)
                    if (newState) {
                        startUpdateRepositoryWorkUseCase()
                    } else {
                        stopUpdateRepositoryWorkUseCase()
                    }
                }
            ),
            Element(
                id = PREF_UPDATE_REPO_STARTUP_KEY,
                title = resourceProvider.getString(R.string.pref_update_repo_when_open),
                switchState = preferencesProvider.updateRepoWhenOpenRepositoryScreen,
                onClick = {
                    val newState = !preferencesProvider.updateRepoWhenOpenRepositoryScreen
                    preferencesProvider.updateRepoWhenOpenRepositoryScreen = newState
                    updateSwitchState(id = PREF_UPDATE_REPO_STARTUP_KEY, switchState = newState)
                }
            ),
            Divider,
            Category(
                title = resourceProvider.getString(R.string.prefs_category_interface),
            ),
            Element(
                id = PREF_APP_THEME_KEY,
                icon = R.drawable.ic_theme_light_dark_24dp,
                title = resourceProvider.getString(R.string.prefs_theme),
                description = defaultAppThemeName,
                onClick = ::showAppThemeSelectionDialog,
            ),
            if (platformInfo.isDynamicColorsAvailable) {
                Element(
                    id = PREF_DYNAMIC_COLORS_KEY,
                    icon = R.drawable.ic_palette_24dp,
                    title = resourceProvider.getString(R.string.prefs_dynamic_colors),
                    description = resourceProvider.getString(R.string.prefs_dynamic_colors_summary),
                    switchState = preferencesProvider.dynamicColors,
                    onClick = {
                        val newState = !preferencesProvider.dynamicColors
                        preferencesProvider.dynamicColors = newState
                        updateSwitchState(id = PREF_DYNAMIC_COLORS_KEY, switchState = newState)
                    }
                )
            } else null,
        )
    }

    private fun updateSettingItem(id: String, block: (item: Element) -> Element) {
        val updatedItems = _items.value.map { item ->
            if (item is Element && item.id == id) block(item) else item
        }
        _items.value = updatedItems
    }

    private fun updateSwitchState(id: String, switchState: Boolean) {
        updateSettingItem(id) { item -> item.copy(switchState = switchState) }
    }

    private fun updateEnabledState(id: String, enabled: Boolean) {
        updateSettingItem(id) { item -> item.copy(isEnabled = enabled) }
    }

    private fun updateDescription(id: String, description: String) {
        updateSettingItem(id) { item -> item.copy(description = description) }
    }

    private fun buildKeyNameMap(keys: Array<String>, names: Array<String>): Map<String, String> =
        buildMap {
            keys.forEachIndexed { index, key ->
                put(key, names.getOrNull(index) ?: "")
            }
        }

    private fun Map<String, String>.toRadioButtonModels(defaultKey: String) =
        map { (key, name) ->
            RadioButtonModel(
                id = key,
                text = name,
                isChecked = key == defaultKey
            )
        }

    private fun showInsteadThemesSelectionDialog() {
        if (insteadThemes.isNotEmpty()) {
            val defaultTheme = preferencesProvider.defaultInsteadTheme
            val buttons = insteadThemes.map { theme ->
                RadioButtonModel(
                    id = theme,
                    text = theme,
                    isChecked = theme == defaultTheme
                )
            }
            _showDialog.value = CustomDialogModel.RadioButtonDialogModel(
                title = resourceProvider.getString(R.string.prefs_default_theme_title),
                buttons = buttons,
                onChoose = { buttonId ->
                    preferencesProvider.defaultInsteadTheme = buttonId
                    buttons.find { it.id == buttonId }?.text?.also { text ->
                        updateDescription(id = PREF_DEFAULT_THEME_KEY, description = text)
                    }
                }
            )
        }
    }

    private fun showTextSizeInputDialog() {
        val defaultTextSize = preferencesProvider.defaultInsteadTextSize
        _showDialog.value = CustomDialogModel.EditTextDialogModel(
            title = resourceProvider.getString(R.string.prefs_text_size_title),
            initialText = defaultTextSize,
            digitsOnly = true,
            onTextChanged = { newText ->
                preferencesProvider.defaultInsteadTextSize = newText
            }
        )
    }

    private fun showKeyboardButtonPositionSelectionDialog() {
        val defaultKey = preferencesProvider.keyboardButtonPosition
        val buttons = keyboardPositions.toRadioButtonModels(defaultKey)
        _showDialog.value = CustomDialogModel.RadioButtonDialogModel(
            title = resourceProvider.getString(R.string.prefs_keyboard_button_title),
            buttons = buttons,
            onChoose = { buttonId ->
                preferencesProvider.keyboardButtonPosition = buttonId
                buttons.find { it.id == buttonId }?.text?.also { text ->
                    updateDescription(id = PREF_KEYBOARD_BUTTON_KEY, description = text)
                }
            }
        )
    }

    private fun showBackButtonBehaviorSelectionDialog() {
        val defaultKey = preferencesProvider.backButton
        val buttons = backButtonBehavior.toRadioButtonModels(defaultKey)
        _showDialog.value = CustomDialogModel.RadioButtonDialogModel(
            title = resourceProvider.getString(R.string.prefs_back_button_title),
            buttons = buttons,
            onChoose = { buttonId ->
                preferencesProvider.backButton = buttonId
                buttons.find { it.id == buttonId }?.text?.also { text ->
                    updateDescription(id = PREF_BACK_BUTTON_KEY, description = text)
                }
            }
        )
    }

    private fun showRepoUrlInputDialog() {
        _showDialog.value = CustomDialogModel.EditTextDialogModel(
            title = resourceProvider.getString(R.string.prefs_repository_title),
            initialText = preferencesProvider.repositoryUrl,
            onTextChanged = { newText ->
                preferencesProvider.repositoryUrl = newText.ifBlank {
                    PreferencesProvider.DEFAULT_REPOSITORY_URL
                }
            }
        )
    }

    private fun showSandboxUrlInputDialog() {
        _showDialog.value = CustomDialogModel.EditTextDialogModel(
            title = resourceProvider.getString(R.string.prefs_sandbox),
            initialText = preferencesProvider.sandboxUrl,
            onTextChanged = { newText ->
                preferencesProvider.sandboxUrl = newText.ifBlank {
                    PreferencesProvider.SANDBOX_REPOSITORY_URL
                }
            }
        )
    }

    private fun showAppThemeSelectionDialog() {
        val defaultKey = preferencesProvider.appTheme
        val buttons = appThemes.toRadioButtonModels(defaultKey)
        _showDialog.value = CustomDialogModel.RadioButtonDialogModel(
            title = resourceProvider.getString(R.string.prefs_theme),
            buttons = buttons,
            onChoose = { buttonId ->
                viewModelScope.launch {
                    preferencesProvider.appTheme = buttonId
                    _changeAppTheme.emit(buttonId)
                    buttons.find { it.id == buttonId }?.text?.also { text ->
                        updateDescription(id = PREF_APP_THEME_KEY, description = text)
                    }
                }
            }
        )
    }
}