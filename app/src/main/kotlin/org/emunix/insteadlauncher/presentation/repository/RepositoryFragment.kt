/*
 * Copyright (c) 2018-2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@AndroidEntryPoint
class RepositoryFragment : Fragment() {

    private val viewModel: RepositoryViewModel by viewModels()

    private val defaultContract = ActivityResultContracts.StartActivityForResult()

    private val repoResult = registerForActivityResult(defaultContract) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.installGame(uri)
            }
        }
    }

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
                val errorDialog by viewModel.showErrorDialog.collectAsState()

                InsteadLauncherTheme {
                    RepositoryScreen(
                        state = state,
                        onBackClick = { findNavController().popBackStack() },
                        onSearchClick = { findNavController().navigate(R.id.action_repositoryFragment_to_searchFragment) },
                        onUpdateRepositoryClick = { viewModel.updateRepository() },
                        onInstallFromZipClick = ::chooseZip,
                        onGameClick = { gameName ->
                            val bundle = bundleOf("game_name" to gameName)
                            findNavController().navigate(R.id.action_repositoryFragment_to_gameFragment, bundle)
                        }
                    )
                    errorDialog?.let { dialogModel ->
                        ErrorDialog(
                            model = dialogModel,
                            onDismiss = viewModel::onErrorDialogDismiss
                        )
                    }
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init()
        handleApplicationZipArgument()
    }

    private fun handleApplicationZipArgument() {
        val appArgumentViewModel: AppArgumentViewModel by activityViewModels()
        appArgumentViewModel.zipUri?.let { uri ->
            viewModel.installGame(uri)
            appArgumentViewModel.zipUri = null
        }
    }

    private fun chooseZip() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"
        repoResult.launch(intent)
    }
}
