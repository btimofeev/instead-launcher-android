/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.helpers.ThemeHelper
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var startUpdateRepositoryWorkUseCase: StartUpdateRepositoryWorkUseCase

    @Inject
    lateinit var stopUpdateRepositoryWorkUseCase: StopUpdateRepositoryWorkUseCase

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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
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
                    ThemeHelper.applyTheme(theme.value)
                }
            }
            "pref_update_repo_background" -> {
                val pref: SwitchPreference? = findPreference("pref_update_repo_background")
                if (pref != null && pref.isChecked) {
                    startUpdateRepositoryWorkUseCase.execute()
                } else {
                    stopUpdateRepositoryWorkUseCase.execute()
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
}
