/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_repository.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.ui.game.GameActivity

class RepositoryFragment : Fragment() {
    private lateinit var viewModel: RepositoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_repository, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProviders.of(activity!!).get(RepositoryViewModel::class.java)

        try_again_button.setOnClickListener { viewModel.updateRepository() }

        viewModel.getGames().observe(this, Observer { games ->
            if (games != null) {
                if (!games.isEmpty()) {
                    list.adapter = RepositoryAdapter(games) {
                        val intent = Intent(activity, GameActivity::class.java)
                        val gameName = it.name
                        intent.putExtra("game_name", gameName)
                        startActivity(intent)
                    }
                } else {
                    viewModel.updateRepository()
                }
            }
        })

        viewModel.getProgressState().observe(this, Observer { state ->
            if (state != null) {
                progress.visible(state)
            }
        })

        viewModel.getErrorViewState().observe(this, Observer { state ->
            if (state != null)
                error_view.visible(state)
        })

        viewModel.getGameListState().observe(this, Observer { state ->
            if (state != null)
                list.visible(state)
        })
    }
}
