/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_repository.*
import kotlinx.android.synthetic.main.fragment_repository.list
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.ui.game.GameActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import org.emunix.insteadlauncher.helpers.showToast


class RepositoryFragment : Fragment() {
    private lateinit var viewModel: RepositoryViewModel
    private lateinit var installDialog: ProgressDialog

    private val listAdapter = RepositoryAdapter { game, image ->
        val intent = Intent(activity, GameActivity::class.java)
        intent.putExtra("game_name", game.name)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as Activity, image as View, game.name)
        startActivity(intent, options.toBundle())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_repository, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(list.context, R.dimen.installed_game_fragment_inset_divider_margin_start)
        dividerItemDecoration.setDrawable(insetDivider)
        list.addItemDecoration(dividerItemDecoration)
        listAdapter.setHasStableIds(true)
        list.adapter = listAdapter
        list.setHasFixedSize(true)

        viewModel = ViewModelProvider(requireActivity()).get(RepositoryViewModel::class.java)

        try_again_button.setOnClickListener { viewModel.updateRepository() }

        swipe_to_refresh.setOnRefreshListener { viewModel.updateRepository() }

        viewModel.getGames().observe(viewLifecycleOwner, Observer { games ->
            if (games != null) {
                if (!games.isEmpty()) {
                    showGames(games)
                } else {
                    viewModel.updateRepository()
                }
            }
        })

        viewModel.getProgressState().observe(viewLifecycleOwner, Observer { state ->
            if (state != null) {
                swipe_to_refresh.isRefreshing = state
            }
        })

        viewModel.getErrorViewState().observe(viewLifecycleOwner, Observer { state ->
            if (state != null)
                error_view.visible(state)
        })

        viewModel.getGameListState().observe(viewLifecycleOwner, Observer { state ->
            if (state != null)
                list.visible(state)
        })

        installDialog = ProgressDialog(activity)
        installDialog.setMessage(activity?.getString(R.string.notification_install_game))
        installDialog.setCancelable(false)
        installDialog.setCanceledOnTouchOutside(false)

        viewModel.getInstallGameDialogState().observe(viewLifecycleOwner, Observer { state ->
            if (state != null) {
                if (state == true)
                    installDialog.show()
                else
                    installDialog.cancel()
            }
        })

        viewModel.getSnackbarMessage().observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { resId ->
                Snackbar.make(list, getString(resId), Snackbar.LENGTH_LONG).show()
            }
        })

        viewModel.getToastMessage().observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { resId ->
                context?.showToast(getString(resId))
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val searchView = menu.findItem(R.id.action_search)!!.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                search(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                search(query)
                return false
            }

            fun search(text: String) {
                val query = "%$text%"
                viewModel.searchGames(query).observe(this@RepositoryFragment, Observer { games ->
                    if (games != null) {
                        showGames(games)
                        nothing_found_text.visible(games.isEmpty())
                    }
                })
            }
        })
    }

    private fun showGames(games: List<Game>) {
        val sortedGames = games.sortedByDescending { it.date }
        listAdapter.submitList(sortedGames)
    }
}
