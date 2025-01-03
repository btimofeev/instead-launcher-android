/*
 * Copyright (c) 2021, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_preferences.preferences_provider

interface PreferencesProvider {

    val isMusicEnabled: Boolean

    val isCursorEnabled: Boolean

    val isOwnGameThemeEnabled: Boolean

    val defaultInsteadTheme: String

    val isHiresEnabled: Boolean

    val defaultInsteadTextSize: String

    val keyboardButtonPosition: String

    val backButton: String

    val isGLHackEnabled: Boolean

    val repositoryUrl: String

    val isSandboxEnabled: Boolean

    val sandboxUrl: String

    val updateRepoInBackground: Boolean

    val updateRepoWhenOpenRepositoryScreen: Boolean

    val appTheme: String

    var resourcesLastUpdate: Long

    companion object {

        const val DEFAULT_REPOSITORY_URL = "http://instead-games.ru/xml.php"
        const val SANDBOX_REPOSITORY_URL = "http://instead-games.ru/xml2.php"

        const val DEFAULT_THEME = "default"

        const val DEFAULT_INSTEAD_THEME = "mobile"
        const val DEFAULT_INSTEAD_TEXT_SIZE = "130"

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
        const val PREF_ENABLE_GAME_THEME_KEY = "pref_enable_game_theme"
        const val PREF_GL_HACK_KEY = "pref_gl_hack"
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