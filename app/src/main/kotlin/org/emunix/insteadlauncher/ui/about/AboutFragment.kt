/*
 * Copyright (c) 2019-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.BuildConfig
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.databinding.FragmentAboutBinding
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : Fragment() {

    @Inject
    lateinit var appVersion: AppVersionRepository

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
