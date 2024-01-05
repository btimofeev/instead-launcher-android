/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_preferences.preferences_provider

import android.content.SharedPreferences
import androidx.core.content.edit
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.BACK_BUTTON_EXIT_GAME
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_INSTEAD_THEME
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_KEYBOARD_BUTTON_POSITION
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_REPOSITORY_URL
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_THEME
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.SANDBOX_REPOSITORY_URL
import javax.inject.Inject

class PreferencesProviderImpl @Inject constructor(private val preferences: SharedPreferences) : PreferencesProvider {

    override val isMusicEnabled: Boolean
        get() = preferences.getBoolean("pref_music", true)

    override val isCursorEnabled: Boolean
        get() = preferences.getBoolean("pref_cursor", false)

    override val isOwnGameThemeEnabled: Boolean
        get() = preferences.getBoolean("pref_enable_game_theme", true)

    override val defaultInsteadTheme: String
        get() = preferences.getString("pref_default_theme", null) ?: DEFAULT_INSTEAD_THEME

    override val isHiresEnabled: Boolean
        get() = preferences.getBoolean("pref_hires", true)

    override val defaultInsteadTextSize: String
        get() = preferences.getString("pref_text_size", null) ?: DEFAULT_INSTEAD_TEXT_SIZE

    override val keyboardButtonPosition: String
        get() = preferences.getString("pref_keyboard_button", null) ?: DEFAULT_KEYBOARD_BUTTON_POSITION

    override val backButton: String
        get() = preferences.getString("pref_back_button", null) ?: BACK_BUTTON_EXIT_GAME

    override val isGLHackEnabled: Boolean
        get() = preferences.getBoolean("pref_gl_hack", false)

    override val repositoryUrl: String
        get() = preferences.getString("pref_repository", null) ?: DEFAULT_REPOSITORY_URL

    override val isSandboxEnabled: Boolean
        get() = preferences.getBoolean("pref_sandbox_enabled", false)

    override val sandboxUrl: String
        get() = preferences.getString("pref_sandbox", null) ?: SANDBOX_REPOSITORY_URL

    override val isRedirectToHttp: Boolean
        get() = preferences.getBoolean("pref_redirect_http", false)

    override val updateRepoInBackground: Boolean
        get() = preferences.getBoolean("pref_update_repo_background", true)

    override val updateRepoWhenOpenRepositoryScreen: Boolean
        get() = preferences.getBoolean("pref_update_repo_startup", false)

    override val appTheme: String
        get() = preferences.getString("app_theme", null) ?: DEFAULT_THEME

    override var resourcesLastUpdate: Long
        get() = preferences.getLong("resources_last_update", -1)
        set(value) {
            preferences.edit { putLong("resources_last_update", value) }
        }
}