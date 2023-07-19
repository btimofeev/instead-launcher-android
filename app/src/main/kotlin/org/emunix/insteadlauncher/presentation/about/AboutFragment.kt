/*
 * Copyright (c) 2019-2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.about

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.BuildConfig
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.databinding.FragmentAboutBinding
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : Fragment(R.layout.fragment_about) {

    @Inject
    lateinit var appVersion: AppVersionRepository

    private val binding by viewBinding(FragmentAboutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.aboutInstead.text = getString(R.string.about_activity_about_instead, BuildConfig.INSTEAD_VERSION)
        binding.aboutInsteadLauncher.text =
            getString(R.string.about_activity_about_instead_launcher, appVersion.versionName)
    }
}
