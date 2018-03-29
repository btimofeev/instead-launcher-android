package org.emunix.insteadlauncher.ui.repository

import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

        viewModel = ViewModelProviders.of(this).get(RepositoryViewModel::class.java)

        viewModel.getGames().observe(this, Observer { games ->
            showEmptyView(true)
            if (games != null) {
                if (!games.isEmpty()) {
                    showEmptyView(false)
                    list.adapter = RepositoryAdapter(games) {
                        val intent = Intent(activity, GameActivity::class.java)
                        val gameName = it.name
                        intent.putExtra("game_name", gameName)
                        startActivity(intent)
                    }
                }
            }
        })

        viewModel.getProgressState().observe(this, Observer { value ->
            if (value != null) {
                progress.visible(value)
            } else {
                progress.visible(false)
            }
        })
    }

    private fun showEmptyView(isActive: Boolean) {
        empty.visible(isActive)
        list.visible(!isActive)
    }
}
