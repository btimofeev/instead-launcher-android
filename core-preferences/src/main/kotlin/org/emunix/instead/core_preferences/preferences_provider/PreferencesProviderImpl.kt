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

    override var isMusicEnabled: Boolean
        get() = preferences.getBoolean(PREF_MUSIC_KEY, true)
        set(value) = preferences.edit { putBoolean(PREF_MUSIC_KEY, value) }

    override var isCursorEnabled: Boolean
        get() = preferences.getBoolean(PREF_CURSOR_KEY, false)
        set(value) = preferences.edit { putBoolean(PREF_CURSOR_KEY, value) }

    override var isOwnGameThemeEnabled: Boolean
        get() = preferences.getBoolean(PREF_ENABLE_GAME_THEME_KEY, true)
        set(value) = preferences.edit { putBoolean(PREF_ENABLE_GAME_THEME_KEY, value) }

    override var defaultInsteadTheme: String
        get() = preferences.getString(PREF_DEFAULT_THEME_KEY, null) ?: DEFAULT_INSTEAD_THEME
        set(value) = preferences.edit { putString(PREF_DEFAULT_THEME_KEY, value) }

    override var isHiresEnabled: Boolean
        get() = preferences.getBoolean(PREF_HIRES_KEY, true)
        set(value) = preferences.edit { putBoolean(PREF_HIRES_KEY, value) }

    override var defaultInsteadTextSize: String
        get() = preferences.getString(PREF_TEXT_SIZE_KEY, null) ?: DEFAULT_INSTEAD_TEXT_SIZE
        set(value) = preferences.edit { putString(PREF_TEXT_SIZE_KEY, value) }

    override var keyboardButtonPosition: String
        get() = preferences.getString(PREF_KEYBOARD_BUTTON_KEY, null) ?: DEFAULT_KEYBOARD_BUTTON_POSITION
        set(value) = preferences.edit { putString(PREF_KEYBOARD_BUTTON_KEY, value) }

    override var backButton: String
        get() = preferences.getString(PREF_BACK_BUTTON_KEY, null) ?: BACK_BUTTON_EXIT_GAME
        set(value) = preferences.edit { putString(PREF_BACK_BUTTON_KEY, value) }

    override var isGLHackEnabled: Boolean
        get() = preferences.getBoolean(PREF_GL_HACK_KEY, false)
        set(value) = preferences.edit { putBoolean(PREF_GL_HACK_KEY, value) }

    override var repositoryUrl: String
        get() = preferences.getString(PREF_REPOSITORY_KEY, null) ?: DEFAULT_REPOSITORY_URL
        set(value) = preferences.edit { putString(PREF_REPOSITORY_KEY, value) }

    override var isSandboxEnabled: Boolean
        get() = preferences.getBoolean(PREF_SANDBOX_ENABLED_KEY, false)
        set(value) = preferences.edit { putBoolean(PREF_SANDBOX_ENABLED_KEY, value) }

    override var sandboxUrl: String
        get() = preferences.getString(PREF_SANDBOX_KEY, null) ?: SANDBOX_REPOSITORY_URL
        set(value) = preferences.edit { putString(PREF_SANDBOX_KEY, value) }

    override var updateRepoInBackground: Boolean
        get() = preferences.getBoolean(PREF_UPDATE_REPO_BACKGROUND_KEY, true)
        set(value) = preferences.edit { putBoolean(PREF_UPDATE_REPO_BACKGROUND_KEY, value) }

    override var updateRepoWhenOpenRepositoryScreen: Boolean
        get() = preferences.getBoolean(PREF_UPDATE_REPO_STARTUP_KEY, false)
        set(value) = preferences.edit { putBoolean(PREF_UPDATE_REPO_STARTUP_KEY, value) }

    override var appTheme: String
        get() = preferences.getString(PREF_APP_THEME_KEY, null) ?: DEFAULT_THEME
        set(value) = preferences.edit { putString(PREF_APP_THEME_KEY, value) }

    override var resourcesLastUpdate: Long
        get() = preferences.getLong(PREF_RESOURCES_LAST_UPDATE_KEY, -1)
        set(value) {
            preferences.edit { putLong(PREF_RESOURCES_LAST_UPDATE_KEY, value) }
        }
}