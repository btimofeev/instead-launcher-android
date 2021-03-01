/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.ui.game.GameActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import org.emunix.insteadlauncher.databinding.FragmentRepositoryBinding
import org.emunix.insteadlauncher.helpers.showToast


class RepositoryFragment : Fragment() {
    private lateinit var viewModel: RepositoryViewModel
    private lateinit var installDialog: ProgressDialog

    private var _binding: FragmentRepositoryBinding? = null
    private val binding get() = _binding!!

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
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRepositoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.list.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(binding.list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(binding.list.context, R.dimen.installed_game_fragment_inset_divider_margin_start)
        dividerItemDecoration.setDrawable(insetDivider)
        binding.list.addItemDecoration(dividerItemDecoration)
        listAdapter.setHasStableIds(true)
        binding.list.adapter = listAdapter
        binding.list.setHasFixedSize(true)

        viewModel = ViewModelProvider(requireActivity()).get(RepositoryViewModel::class.java)

        binding.tryAgainButton.setOnClickListener { viewModel.updateRepository() }

        binding.swipeToRefresh.setOnRefreshListener { viewModel.updateRepository() }

        viewModel.getGames().observe(viewLifecycleOwner) { games ->
            if (games != null) {
                if (!games.isEmpty()) {
                    showGames(games)
                } else {
                    viewModel.updateRepository()
                }
            }
        }

        viewModel.getProgressState().observe(viewLifecycleOwner) { state ->
            if (state != null) {
                binding.swipeToRefresh.isRefreshing = state
            }
        }

        viewModel.getErrorViewState().observe(viewLifecycleOwner) { state ->
            if (state != null)
                binding.errorView.visible(state)
        }

        viewModel.getGameListState().observe(viewLifecycleOwner) { state ->
            if (state != null)
                binding.list.visible(state)
        }

        installDialog = ProgressDialog(activity)
        installDialog.setMessage(activity?.getString(R.string.notification_install_game))
        installDialog.setCancelable(false)
        installDialog.setCanceledOnTouchOutside(false)

        viewModel.getInstallGameDialogState().observe(viewLifecycleOwner) { state ->
            if (state != null) {
                if (state == true)
                    installDialog.show()
                else
                    installDialog.cancel()
            }
        }

        viewModel.getSnackbarMessage().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resId ->
                Snackbar.make(binding.list, getString(resId), Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.getToastMessage().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resId ->
                context?.showToast(getString(resId))
            }
        }
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
                viewModel.searchGames(query).observe(this@RepositoryFragment) { games ->
                    if (games != null) {
                        showGames(games)
                        binding.nothingFoundText.visible(games.isEmpty())
                    }
                }
            }
        })
    }

    private fun showGames(games: List<Game>) {
        val sortedGames = games.sortedByDescending { it.date }
        listAdapter.submitList(sortedGames)
    }
}
