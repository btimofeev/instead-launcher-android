/*
 * Copyright (c) 2019-2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@AndroidEntryPoint
class InstalledGamesFragment : Fragment() {

    private val appArgumentViewModel: AppArgumentViewModel by activityViewModels()

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
                    InstalledGamesScreen(
                        navigateToGameInfoScreen = ::navigateToGameInfoScreen,
                        navigateToSettingsScreen = ::navigateToSettingsScreen,
                        navigateToAboutAppScreen = ::navigateToAboutAppScreen,
                        navigateToRepositoryScreen = ::navigateToRepositoryScreen
                    )
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (appArgumentViewModel.zipUri != null) {
            navigateToRepositoryScreen()
        }
    }

    private fun navigateToRepositoryScreen() {
        findNavController().navigate(R.id.action_installedGamesFragment_to_repositoryFragment)
    }

    private fun navigateToGameInfoScreen(gameName: String) {
        val bundle = bundleOf("game_name" to gameName)
        findNavController().navigate(R.id.action_installedGamesFragment_to_gameFragment, bundle)
    }

    private fun navigateToSettingsScreen() {
        findNavController().navigate(R.id.action_installedGamesFragment_to_settingsFragment)
    }

    private fun navigateToAboutAppScreen() {
        findNavController().navigate(R.id.action_installedGamesFragment_to_aboutFragment)
    }
}