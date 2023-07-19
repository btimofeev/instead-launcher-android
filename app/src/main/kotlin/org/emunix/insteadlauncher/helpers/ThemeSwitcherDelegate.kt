/*
 * Copyright (c) 2019, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import org.emunix.insteadlauncher.helpers.ThemeSwitcherDelegate.Theme.DARK
import org.emunix.insteadlauncher.helpers.ThemeSwitcherDelegate.Theme.DEFAULT
import org.emunix.insteadlauncher.helpers.ThemeSwitcherDelegate.Theme.LIGHT

internal class ThemeSwitcherDelegate {

    fun applyTheme(themeName: String) {
        when (getThemeByName(themeName)) {
            LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            DEFAULT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

    private fun getThemeByName(name: String): Theme = try {
        Theme.valueOf(name.uppercase())
    } catch (e: IllegalArgumentException) {
        e.writeToLog()
        DEFAULT
    }

    private enum class Theme {

        LIGHT,

        DARK,

        DEFAULT;
    }
}