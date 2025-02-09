/*
 * Copyright (c) 2018-2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.presentation.dialogs.ErrorDialog
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme
import org.emunix.insteadlauncher.utils.launchBrowser
import javax.inject.Inject

@AndroidEntryPoint
class GameFragment : Fragment() {

    @Inject
    lateinit var gameManager: GameManager

    private val viewModel: GameViewModel by viewModels()

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
                val state by viewModel.state.collectAsState()
                val closeCommand by viewModel.closeScreenCommand.collectAsState(initial = null)
                val errorDialog by viewModel.showErrorDialog.collectAsState()
                val deleteGameName by viewModel.showDeleteGameDialog.collectAsState()

                if (closeCommand != null) {
                    closeScreen()
                }

                InsteadLauncherTheme {
                    GameInfoScreen(
                        state = state,
                        onBackClick = { findNavController().popBackStack() },
                        onInstallClick = viewModel::installGame,
                        onRunClick = viewModel::runGame,
                        onUpdateClick = viewModel::installGame,
                        onDeleteClick = viewModel::onDeleteGameClicked,
                        onFeedbackClick = { activity?.launchBrowser(state.siteUrl) },
                    )
                    deleteGameName?.let { gameName ->
                        DeleteGameDialog(
                            onConfirm = { viewModel.onDeleteGameConfirmed(gameName) },
                            onDismiss = { viewModel.onDeleteGameRejected() }
                        )
                    }
                    errorDialog?.let { dialog ->
                        ErrorDialog(
                            message = dialog.message,
                            onDismiss = { viewModel.onErrorDialogDismissed() }
                        )
                    }
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameName = arguments?.getString("game_name")
            ?: throw IllegalArgumentException("GameFragment require game_name passed as argument")
        viewModel.init(gameName)
    }

    private fun closeScreen() {
        findNavController().popBackStack()
    }
}
