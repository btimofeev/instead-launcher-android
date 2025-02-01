/*
 * Copyright (c) 2021-2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.unpackresources

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.ERROR
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.SUCCESS
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.UNPACKING
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@AndroidEntryPoint
class UnpackResourcesFragment : Fragment() {

    private val viewModel: UnpackResourcesViewModel by viewModels()

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
                val state by viewModel.screenState.collectAsState()

                InsteadLauncherTheme {
                    Scaffold(modifier = Modifier.safeDrawingPadding()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (state) {
                                SUCCESS -> navigateToInstalledGamesScreen()
                                UNPACKING -> UnpackResourcesLoadingScreen()
                                ERROR -> UnpackResourcesErrorScreen(
                                    onTryAgainClick = viewModel::tryAgainIsClicked
                                )
                            }
                        }
                    }
                }
            }
        }
        return view
    }

    private fun navigateToInstalledGamesScreen() {
        findNavController().navigate(R.id.action_unpackResourcesFragment_to_installedGamesFragment)
    }
}