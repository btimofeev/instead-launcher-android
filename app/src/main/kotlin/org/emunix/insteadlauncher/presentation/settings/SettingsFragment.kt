/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
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
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar) as MaterialToolbar
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener {
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
            "pref_repository" -> {
                findPreference<EditTextPreference?>("pref_repository")?.let { repo ->
                    if (repo.text.isNullOrBlank()) {
                        repo.text = PreferencesProvider.DEFAULT_REPOSITORY_URL
                    }
                }
            }

            "pref_sandbox" -> {
                findPreference<EditTextPreference?>("pref_sandbox")?.let { sandbox ->
                    if (sandbox.text.isNullOrBlank()) {
                        sandbox.text = PreferencesProvider.SANDBOX_REPOSITORY_URL
                    }
                }
            }

            "app_theme" -> {
                val theme: ListPreference? = findPreference("app_theme")
                if (theme != null) {
                    ThemeSwitcherDelegate().applyTheme(theme.value)
                }
            }

            "pref_update_repo_background" -> {
                val pref: SwitchPreference? = findPreference("pref_update_repo_background")
                if (pref != null && pref.isChecked) {
                    startUpdateRepositoryWorkUseCase()
                } else {
                    stopUpdateRepositoryWorkUseCase()
                }
            }

            "pref_default_theme" -> {
                findPreference<ListPreference>("pref_default_theme")?.let { pref ->
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
        findPreference<ListPreference>("pref_default_theme")?.let { pref ->
            val savedTheme = preferencesProvider.defaultInsteadTheme
            if (themes.isNotEmpty()) {
                pref.entries = themes
                pref.entryValues = themes
                when {
                    themes.contains(savedTheme) -> pref.value = savedTheme
                    themes.contains("mobile") -> pref.value = "mobile"
                    themes.contains("wide") -> pref.value = "wide"
                    themes.contains("default") -> pref.value = "default"
                    else -> pref.setValueIndex(0)
                }
            } else {
                pref.entries = requireContext().resources.getStringArray(R.array.prefs_themes_entries)
                pref.entryValues = requireContext().resources.getStringArray(R.array.prefs_themes_values)
                pref.setValueIndex(0)
            }
            pref.summary = pref.entry
        }
    }
}
