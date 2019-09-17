/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.ThemeHelper


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        when (key) {
            "pref_repository" -> {
                val repo = findPreference("pref_repository") as EditTextPreference
                if (repo.text.isBlank()) {
                    repo.text = InsteadLauncher.DEFAULT_REPOSITORY
                }
            }
            "pref_sandbox" -> {
                val sandbox = findPreference("pref_sandbox") as EditTextPreference
                if (sandbox.text.isBlank()) {
                    sandbox.text = InsteadLauncher.SANDBOX
                }
            }
            "app_theme" -> {
                val theme = findPreference("app_theme") as ListPreference
                ThemeHelper.applyTheme(theme.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
