/*
 * Copyright (c) 2021, 2025 Boris Timofeev <btimofeev@emunix.org>
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
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_APP_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_BACK_BUTTON_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_CURSOR_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_DEFAULT_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_ENABLE_GAME_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_GL_HACK_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_HIRES_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_KEYBOARD_BUTTON_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_MUSIC_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_REPOSITORY_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_RESOURCES_LAST_UPDATE_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_SANDBOX_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_SANDBOX_ENABLED_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_TEXT_SIZE_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_UPDATE_REPO_BACKGROUND_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_UPDATE_REPO_STARTUP_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.SANDBOX_REPOSITORY_URL
import javax.inject.Inject

class PreferencesProviderImpl @Inject constructor(private val preferences: SharedPreferences) : PreferencesProvider {

    override val isMusicEnabled: Boolean
        get() = preferences.getBoolean(PREF_MUSIC_KEY, true)

    override val isCursorEnabled: Boolean
        get() = preferences.getBoolean(PREF_CURSOR_KEY, false)

    override val isOwnGameThemeEnabled: Boolean
        get() = preferences.getBoolean(PREF_ENABLE_GAME_THEME_KEY, true)

    override val defaultInsteadTheme: String
        get() = preferences.getString(PREF_DEFAULT_THEME_KEY, null) ?: DEFAULT_INSTEAD_THEME

    override val isHiresEnabled: Boolean
        get() = preferences.getBoolean(PREF_HIRES_KEY, true)

    override val defaultInsteadTextSize: String
        get() = preferences.getString(PREF_TEXT_SIZE_KEY, null) ?: DEFAULT_INSTEAD_TEXT_SIZE

    override val keyboardButtonPosition: String
        get() = preferences.getString(PREF_KEYBOARD_BUTTON_KEY, null) ?: DEFAULT_KEYBOARD_BUTTON_POSITION

    override val backButton: String
        get() = preferences.getString(PREF_BACK_BUTTON_KEY, null) ?: BACK_BUTTON_EXIT_GAME

    override val isGLHackEnabled: Boolean
        get() = preferences.getBoolean(PREF_GL_HACK_KEY, false)

    override val repositoryUrl: String
        get() = preferences.getString(PREF_REPOSITORY_KEY, null) ?: DEFAULT_REPOSITORY_URL

    override val isSandboxEnabled: Boolean
        get() = preferences.getBoolean(PREF_SANDBOX_ENABLED_KEY, false)

    override val sandboxUrl: String
        get() = preferences.getString(PREF_SANDBOX_KEY, null) ?: SANDBOX_REPOSITORY_URL

    override val updateRepoInBackground: Boolean
        get() = preferences.getBoolean(PREF_UPDATE_REPO_BACKGROUND_KEY, true)

    override val updateRepoWhenOpenRepositoryScreen: Boolean
        get() = preferences.getBoolean(PREF_UPDATE_REPO_STARTUP_KEY, false)

    override val appTheme: String
        get() = preferences.getString(PREF_APP_THEME_KEY, null) ?: DEFAULT_THEME

    override var resourcesLastUpdate: Long
        get() = preferences.getLong(PREF_RESOURCES_LAST_UPDATE_KEY, -1)
        set(value) {
            preferences.edit { putLong(PREF_RESOURCES_LAST_UPDATE_KEY, value) }
        }
}