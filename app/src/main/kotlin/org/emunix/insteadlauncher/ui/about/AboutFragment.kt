/*
 * Copyright (c) 2019-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.emunix.insteadlauncher.R
import kotlinx.android.synthetic.main.fragment_about.*
import org.emunix.insteadlauncher.BuildConfig
import org.emunix.insteadlauncher.helpers.AppVersion


class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        about_instead.text = getString(R.string.about_activity_about_instead, BuildConfig.INSTEAD_VERSION)
        about_instead_launcher.text = getString(R.string.about_activity_about_instead_launcher, AppVersion(requireContext()).getString())

    }
}
