/*
 * Copyright (c) 2018-2023, 2025-2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@AndroidEntryPoint
class RepositoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_compose, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                InsteadLauncherTheme {
                    val zipUriFromAppArgument = remember { getApplicationZipArgument() }
                    RepositoryScreen(
                        zipUriFromAppArgument = zipUriFromAppArgument,
                        onBackClick = { findNavController().popBackStack() },
                        onSearchClick = { findNavController().navigate(R.id.action_repositoryFragment_to_searchFragment) },
                        onGameClick = { gameName ->
                            val bundle = bundleOf("game_name" to gameName)
                            findNavController().navigate(
                                R.id.action_repositoryFragment_to_gameFragment,
                                bundle
                            )
                        }
                    )
                }
            }
        }
        return view
    }

    private fun getApplicationZipArgument(): Uri? {
        val appArgumentViewModel: AppArgumentViewModel by activityViewModels()
        appArgumentViewModel.zipUri?.let { uri ->
            appArgumentViewModel.zipUri = null
            return uri
        }
        return null
    }
}
