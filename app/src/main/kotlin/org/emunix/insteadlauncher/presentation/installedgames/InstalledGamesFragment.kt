/*
 * Copyright (c) 2019-2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@AndroidEntryPoint
class InstalledGamesFragment : Fragment() {

    private val viewModel: InstalledGamesViewModel by viewModels()
    private val appArgumentViewModel: AppArgumentViewModel by activityViewModels()

    private val gameActions = GameActions(
        onPlayClick = { gameName ->
            viewModel.playGame(gameName = gameName, playFromBeginning = false)
        },
        onPlayFromBeginningClick = { gameName ->
            viewModel.playGame(gameName = gameName, playFromBeginning = true)
        },
        onDeleteClick = { gameName -> viewModel.onDeleteGameClicked(gameName) },
        onAboutClick = { gameName -> navigateToGameInfoScreen(gameName) }
    )

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
                val games by viewModel.gameItems.collectAsState()
                val deleteGameName by viewModel.showDeleteGameDialog.collectAsState()

                InsteadLauncherTheme {
                    InstalledGamesScreen(
                        games = games,
                        onSettingsClick = { navigateToSettingsScreen() },
                        onAboutClick = { navigateToAboutAppScreen() },
                        onAddGameClick = { navigateToRepositoryScreen() },
                        gameActions = gameActions,
                    )
                    if (deleteGameName != null) {
                        DeleteGameDialog(
                            onConfirm = { deleteGameName?.let { viewModel.onDeleteGameConfirmed(it) } },
                            onDismiss = { viewModel.onDeleteGameRejected() }
                        )
                    }
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
        viewModel.init()
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