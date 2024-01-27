/*
 * Copyright (c) 2021-2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.unpackresources

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.databinding.FragmentUnpackResourcesBinding
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.ERROR
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.SUCCESS
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.UNPACKING

@AndroidEntryPoint
class UnpackResourcesFragment : Fragment(R.layout.fragment_unpack_resources) {

    private val binding by viewBinding(FragmentUnpackResourcesBinding::bind)

    private val viewModel: UnpackResourcesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.unpackResourcesTryAgainButton.setOnClickListener { viewModel.tryAgainIsClicked() }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.screenState.collect { state ->
                    when (state) {
                        SUCCESS -> navigateToInstalledGamesScreen()
                        UNPACKING -> showError(false)
                        ERROR -> showError(true)
                    }
                }
            }
        }
    }

    private fun navigateToInstalledGamesScreen() {
        findNavController().navigate(R.id.action_unpackResourcesFragment_to_installedGamesFragment)
    }

    private fun showError(isErrorVisible: Boolean) {
        with(binding) {
            unpackResourcesProgressBar.isVisible = !isErrorVisible
            errorTextView.isVisible = isErrorVisible
            unpackResourcesTryAgainButton.isVisible = isErrorVisible
        }
    }
}