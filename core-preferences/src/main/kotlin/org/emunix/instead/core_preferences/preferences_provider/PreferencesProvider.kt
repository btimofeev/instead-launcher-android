/*
 * Copyright (c) 2021, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_preferences.preferences_provider

import kotlinx.coroutines.flow.Flow

interface PreferencesProvider {

    var isMusicEnabled: Boolean

    var isCursorEnabled: Boolean

    var isOwnGameThemeEnabled: Boolean

    var defaultInsteadTheme: String

    val isDefaultInsteadThemeSet: Boolean

    var isHiresEnabled: Boolean

    var defaultInsteadTextSize: String

    val isDefaultInsteadTextSizeSet: Boolean

    var keyboardButtonPosition: String

    val isKeyboardButtonPositionSet: Boolean

    var backButton: String

    var repositoryUrl: String

    var isSandboxEnabled: Boolean

    var sandboxUrl: String

    var updateRepoInBackground: Boolean

    var updateRepoWhenOpenRepositoryScreen: Boolean

    var appTheme: String

    var dynamicColors: Boolean

    var resourcesLastUpdate: Long

    fun observeDynamicColorsPrefChanges(): Flow<Boolean>

    companion object {

        const val DEFAULT_REPOSITORY_URL = "http://instead-games.ru/xml.php"
        const val SANDBOX_REPOSITORY_URL = "http://instead-games.ru/xml2.php"

        const val DEFAULT_THEME = "default"

        const val INSTEAD_THEME_DEFAULT = "default"
        const val INSTEAD_THEME_MOBILE = "mobile"
        const val INSTEAD_THEME_WIDE = "wide"

        const val DEFAULT_INSTEAD_THEME = INSTEAD_THEME_MOBILE
        const val DEFAULT_INSTEAD_TEXT_SIZE = "130"
        const val LARGE_SCREEN_INSTEAD_TEXT_SIZE = "100"

        // Bounds and granularity of defaultInsteadTextSize, from FONT_MIN_SZ and FONT_MAX_SZ in the engine.
        const val MIN_INSTEAD_TEXT_SIZE = 50
        const val MAX_INSTEAD_TEXT_SIZE = 400
        const val INSTEAD_TEXT_SIZE_STEP = 10

        const val DEFAULT_DYNAMIC_COLORS = true

        const val DEFAULT_KEYBOARD_BUTTON_POSITION = "bottom_left"
        const val KEYBOARD_BUTTON_BOTTOM_LEFT = "bottom_left"
        const val KEYBOARD_BUTTON_BOTTOM_CENTER = "bottom_center"
        const val KEYBOARD_BUTTON_BOTTOM_RIGHT = "bottom_right"
        const val KEYBOARD_BUTTON_LEFT = "left"
        const val KEYBOARD_BUTTON_RIGHT = "right"
        const val KEYBOARD_BUTTON_TOP_LEFT = "top_left"
        const val KEYBOARD_BUTTON_TOP_CENTER = "top_center"
        const val KEYBOARD_BUTTON_TOP_RIGHT = "top_right"
        const val KEYBOARD_DO_NOT_SHOW_BUTTON = "do_not_show_button"

        const val BACK_BUTTON_EXIT_GAME = "exit_game"
        const val BACK_BUTTON_OPEN_MENU = "open_menu"

        const val PREF_APP_THEME_KEY = "app_theme"
        const val PREF_BACK_BUTTON_KEY = "pref_back_button"
        const val PREF_CURSOR_KEY = "pref_cursor"
        const val PREF_DEFAULT_THEME_KEY = "pref_default_theme"
        const val PREF_DYNAMIC_COLORS_KEY = "pref_dynamic_colors"
        const val PREF_ENABLE_GAME_THEME_KEY = "pref_enable_game_theme"
        const val PREF_HIRES_KEY = "pref_hires"
        const val PREF_MUSIC_KEY = "pref_music"
        const val PREF_KEYBOARD_BUTTON_KEY = "pref_keyboard_button"
        const val PREF_REPOSITORY_KEY = "pref_repository"
        const val PREF_RESOURCES_LAST_UPDATE_KEY = "resources_last_update"
        const val PREF_SANDBOX_KEY = "pref_sandbox"
        const val PREF_SANDBOX_ENABLED_KEY = "pref_sandbox_enabled"
        const val PREF_TEXT_SIZE_KEY = "pref_text_size"
        const val PREF_UPDATE_REPO_BACKGROUND_KEY = "pref_update_repo_background"
        const val PREF_UPDATE_REPO_STARTUP_KEY = "pref_update_repo_startup"
    }
}