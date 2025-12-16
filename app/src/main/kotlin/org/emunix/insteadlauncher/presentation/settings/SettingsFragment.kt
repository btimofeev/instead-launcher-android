/*
 * Copyright (c) 2018-2021, 2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.dialogs.CustomDialog
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme
import org.emunix.insteadlauncher.utils.ThemeSwitcherDelegate

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compose, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val items by viewModel.items.collectAsState()
                val showDialog by viewModel.showDialog.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.changeAppTheme.collect { themeName ->
                        ThemeSwitcherDelegate().applyTheme(themeName)
                    }
                }

                InsteadLauncherTheme {
                    SettingsScreen(
                        items = items,
                        onBackClick = { findNavController().popBackStack() } ,
                    )
                    showDialog?.let { dialogModel ->
                        CustomDialog(
                            model = dialogModel,
                            onCloseDialog = viewModel::onDialogClosed,
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
    }
}
