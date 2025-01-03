/*
 * Copyright (c) 2018-2021, 2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_APP_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_DEFAULT_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_REPOSITORY_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_SANDBOX_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_UPDATE_REPO_BACKGROUND_KEY
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.utils.ThemeSwitcherDelegate
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var startUpdateRepositoryWorkUseCase: StartUpdateRepositoryWorkUseCase

    @Inject
    lateinit var stopUpdateRepositoryWorkUseCase: StopUpdateRepositoryWorkUseCase

    @Inject
    lateinit var fileSystemRepository: FileSystemRepository

    @Inject
    lateinit var preferencesProvider: PreferencesProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById(R.id.toolbar) as? MaterialToolbar
        toolbar?.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupInsteadThemesMenu()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        when (key) {
            PREF_REPOSITORY_KEY -> {
                findPreference<EditTextPreference?>(PREF_REPOSITORY_KEY)?.let { repo ->
                    if (repo.text.isNullOrBlank()) {
                        repo.text = PreferencesProvider.DEFAULT_REPOSITORY_URL
                    }
                }
            }

            PREF_SANDBOX_KEY -> {
                findPreference<EditTextPreference?>(PREF_SANDBOX_KEY)?.let { sandbox ->
                    if (sandbox.text.isNullOrBlank()) {
                        sandbox.text = PreferencesProvider.SANDBOX_REPOSITORY_URL
                    }
                }
            }

            PREF_APP_THEME_KEY -> {
                val theme: ListPreference? = findPreference(PREF_APP_THEME_KEY)
                if (theme != null) {
                    ThemeSwitcherDelegate().applyTheme(theme.value)
                }
            }

            PREF_UPDATE_REPO_BACKGROUND_KEY -> {
                val pref: SwitchPreference? = findPreference(PREF_UPDATE_REPO_BACKGROUND_KEY)
                if (pref != null && pref.isChecked) {
                    startUpdateRepositoryWorkUseCase()
                } else {
                    stopUpdateRepositoryWorkUseCase()
                }
            }

            PREF_DEFAULT_THEME_KEY -> {
                findPreference<ListPreference>(PREF_DEFAULT_THEME_KEY)?.let { pref ->
                    pref.summary = pref.entry
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun setupInsteadThemesMenu() {
        lifecycleScope.launch {
            setInsteadThemes(fileSystemRepository.getInstalledThemeNames().toTypedArray())
        }
    }

    private fun setInsteadThemes(themes: Array<String>) {
        findPreference<ListPreference>(PREF_DEFAULT_THEME_KEY)?.let { pref ->
            if (themes.isNotEmpty()) {
                pref.entries = themes
                pref.entryValues = themes
                pref.selectTheme(themes)
            } else {
                pref.entries = pref.context.resources.getStringArray(R.array.prefs_themes_entries)
                pref.entryValues = pref.context.resources.getStringArray(R.array.prefs_themes_values)
                pref.setValueIndex(0)
            }
            pref.summary = pref.entry
        }
    }

    private fun ListPreference.selectTheme(themes: Array<String>) {
        val savedTheme = preferencesProvider.defaultInsteadTheme
        when {
            themes.contains(savedTheme) -> this.value = savedTheme
            themes.contains(MOBILE) -> this.value = MOBILE
            themes.contains(WIDE) -> this.value = WIDE
            themes.contains(DEFAULT) -> this.value = DEFAULT
            else -> this.setValueIndex(0)
        }
    }

    private companion object {

        private const val MOBILE = "mobile"
        private const val WIDE = "wide"
        private const val DEFAULT = "default"
    }
}
