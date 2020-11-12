/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_unpack_resources.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.visible


class UnpackResourcesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unpack_resources, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProvider(this).get(UnpackResourcesViewModel::class.java)

        viewModel.getErrorStatus().observe(viewLifecycleOwner, { showError ->
            unpackResourcesProgressBar.visible(!showError)
            errorTextView.visible(showError)
            unpackResourcesTryAgainButton.visible(showError)
        })

        unpackResourcesTryAgainButton.setOnClickListener { viewModel.tryAgainIsClicked() }
    }
}