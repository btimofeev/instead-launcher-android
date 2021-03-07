/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.unpackresources

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.databinding.FragmentUnpackResourcesBinding
import org.emunix.insteadlauncher.helpers.visible


class UnpackResourcesFragment : Fragment() {

    private var _binding: FragmentUnpackResourcesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UnpackResourcesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentUnpackResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUnpackSuccessStatus().observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_unpackResourcesFragment_to_installedGamesFragment)
            }
        }

        viewModel.getErrorStatus().observe(viewLifecycleOwner) { showError ->
            binding.unpackResourcesProgressBar.visible(!showError)
            binding.errorTextView.visible(showError)
            binding.unpackResourcesTryAgainButton.visible(showError)
        }

        binding.unpackResourcesTryAgainButton.setOnClickListener { viewModel.tryAgainIsClicked() }
    }
}