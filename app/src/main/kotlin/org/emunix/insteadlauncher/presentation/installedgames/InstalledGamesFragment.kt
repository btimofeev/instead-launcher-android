/*
 * Copyright (c) 2019-2024 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.R.dimen
import org.emunix.insteadlauncher.databinding.FragmentInstalledGamesBinding
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.utils.insetDivider
import javax.inject.Inject

@AndroidEntryPoint
class InstalledGamesFragment : Fragment(R.layout.fragment_installed_games) {

    @Inject
    lateinit var gameManager: GameManager

    private val binding by viewBinding(FragmentInstalledGamesBinding::bind)

    private val viewModel: InstalledGamesViewModel by viewModels()
    private val appArgumentViewModel: AppArgumentViewModel by activityViewModels()

    private lateinit var listAdapter: InstalledGamesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (appArgumentViewModel.zipUri != null) {
            navigateToRepositoryScreen()
        }
        setupViews()
        viewModel.init()
        setupObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_installed_games, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                navigateToSettingsScreen()
                return true
            }

            R.id.action_about -> {
                navigateToAboutAppScreen()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        requireActivity().menuInflater.inflate(R.menu.menu_context_installed_games, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val gameName = listAdapter.longClickedGameName ?: return false
        when (item.itemId) {
            R.id.installed_games_activity_context_menu_play -> {
                viewModel.playGame(gameName)
            }

            R.id.installed_games_activity_context_menu_play_from_beginning -> {
                viewModel.playGame(gameName, true)
            }

            R.id.installed_games_activity_context_menu_delete -> {
                showDeleteGameDialog(gameName)
            }

            R.id.installed_games_activity_context_menu_about -> {
                navigateToGameInfoScreen(gameName)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun setupViews() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.fab.setOnClickListener { navigateToRepositoryScreen() }
        setupGameList()
    }

    private fun setupGameList() {
        binding.list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        setupGameListDecoration()
        setupGameListAdapter()
        binding.list.setHasFixedSize(true)
        registerForContextMenu(binding.list)
    }

    private fun setupGameListDecoration() {
        val dividerItemDecoration = DividerItemDecoration(binding.list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(
            context = binding.list.context,
            startOffsetDimension = dimen.installed_game_fragment_inset_divider_margin_start
        )
        dividerItemDecoration.setDrawable(insetDivider)
        binding.list.addItemDecoration(dividerItemDecoration)
    }

    private fun setupGameListAdapter() {
        listAdapter = InstalledGamesAdapter { viewModel.playGame(it) }
        listAdapter.setHasStableIds(true)
        binding.list.adapter = listAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(State.STARTED) {
                viewModel.gameItems.collect { games ->
                    if (games != null) {
                        listAdapter.submitList(games.toList())
                        binding.emptyView.isVisible = games.isEmpty()
                    }
                }
            }
        }
    }

    private fun showDeleteGameDialog(gameName: String) {
        val deleteDialog = DeleteGameDialog.newInstance(gameName, gameManager)
        if (isAdded) {
            parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
        }
    }

    private fun navigateToRepositoryScreen() {
        findNavController().navigate(R.id.action_installedGamesFragment_to_repositoryFragment)
    }

    private fun navigateToGameInfoScreen(gameName: String) {
        val bundle = bundleOf("game_name" to gameName)
        findNavController().navigate(R.id.action_installedGamesFragment_to_gameFragment, bundle)
    }

    private fun navigateToSettingsScreen() {
        findNavController().navigate(R.id.action_installedGamesFragment_to_settingsFragment)
    }

    private fun navigateToAboutAppScreen() {
        findNavController().navigate(R.id.action_installedGamesFragment_to_aboutFragment)
    }
}