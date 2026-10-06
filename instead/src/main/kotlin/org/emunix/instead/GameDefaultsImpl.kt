/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_KEYBOARD_BUTTON_POSITION
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_THEME_DEFAULT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_THEME_MOBILE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_THEME_WIDE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_DO_NOT_SHOW_BUTTON
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.LARGE_SCREEN_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead_api.GameDefaultsApi
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDefaultsImpl @Inject constructor(
    private val context: Context,
    private val preferenceProvider: PreferencesProvider,
    private val storage: Storage,
) : GameDefaultsApi {

    override fun resolveTheme(): String {
        if (!preferenceProvider.isDefaultInsteadThemeSet) {
            preferenceProvider.defaultInsteadTheme = detectTheme()
        }
        return preferenceProvider.defaultInsteadTheme
    }

    override fun resolveTextScale(): String {
        if (!preferenceProvider.isDefaultInsteadTextSizeSet) {
            preferenceProvider.defaultInsteadTextSize =
                if (isLargeScreen()) LARGE_SCREEN_INSTEAD_TEXT_SIZE else DEFAULT_INSTEAD_TEXT_SIZE
        }
        return preferenceProvider.defaultInsteadTextSize
    }

    override fun resolveKeyboardButtonPosition(): String {
        if (!preferenceProvider.isKeyboardButtonPositionSet) {
            preferenceProvider.keyboardButtonPosition = if (isTelevision()) {
                KEYBOARD_DO_NOT_SHOW_BUTTON
            } else {
                DEFAULT_KEYBOARD_BUTTON_POSITION
            }
        }
        return preferenceProvider.keyboardButtonPosition
    }

    override fun isTelevision(): Boolean = uiModeType() == Configuration.UI_MODE_TYPE_TELEVISION

    private fun uiModeType(): Int? =
        (context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager)?.currentModeType

    private fun detectTheme(): String {
        val landscape = isLargeScreen()
        val preferred = if (landscape) INSTEAD_THEME_WIDE else INSTEAD_THEME_MOBILE
        if (isThemeInstalled(preferred)) {
            return preferred
        }
        val fallback = if (landscape) INSTEAD_THEME_MOBILE else INSTEAD_THEME_WIDE
        return if (isThemeInstalled(fallback)) fallback else INSTEAD_THEME_DEFAULT
    }

    private fun isLargeScreen(): Boolean {
        val uiModeType = uiModeType()
        return uiModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
                uiModeType == Configuration.UI_MODE_TYPE_APPLIANCE ||
                context.resources.configuration.smallestScreenWidthDp >= TABLET_SMALLEST_WIDTH_DP
    }

    private fun isThemeInstalled(name: String): Boolean =
        hasTheme(storage.getThemesDirectory(), name) || hasTheme(
            storage.getUserThemesDirectory(),
            name
        )

    private fun hasTheme(themesDir: File, name: String): Boolean {
        val themeDir = File(themesDir, name)
        return themeDir.isDirectory && File(themeDir, THEME_FILE).exists()
    }

    companion object {
        private const val TABLET_SMALLEST_WIDTH_DP = 600
        private const val THEME_FILE = "theme.ini"
    }
}
