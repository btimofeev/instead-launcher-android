/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.StorageHelper
import java.io.File

class ThemeListPreference
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ListPreference(context, attrs) {
    init {
        val themes = getThemes().toTypedArray()
        if (themes.isNotEmpty()) {
            entries = themes
            entryValues = themes
            when {
                themes.contains("mobile") -> value = "mobile"
                themes.contains("wide") -> value = "wide"
                themes.contains("default") -> value = "default"
                else -> setValueIndex(0)
            }
        } else {
            entries = context.resources.getStringArray(R.array.prefs_themes_entries)
            entryValues = context.resources.getStringArray(R.array.prefs_themes_values)
            setValueIndex(0)
        }
    }

    private fun getThemes(): List<String> {
        val themes = mutableListOf<String>()
        val internalThemesDir = StorageHelper(context).getThemesDirectory()
        val externalThemesDir = StorageHelper(context).getUserThemesDirectory()
        themes.addAll(getThemesFrom(internalThemesDir))
        if (internalThemesDir.canonicalFile != externalThemesDir.canonicalFile) {
            for (theme in getThemesFrom(externalThemesDir)) {
                if (!themes.contains(theme))
                    themes.add(theme)
            }
        }
        return themes
    }

    private fun getThemesFrom(path: File): List<String> {
        val themes = mutableListOf<String>()
        if (path.exists()) {
            val dirs = path.listFiles().filter { it.isDirectory }
            dirs.forEach { dir ->
                if (File(dir, "theme.ini").exists()) {
                    themes.add(dir.name)
                }
            }
        }
        return themes
    }
}
