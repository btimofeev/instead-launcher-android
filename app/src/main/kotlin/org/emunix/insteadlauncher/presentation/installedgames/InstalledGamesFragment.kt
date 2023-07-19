/*
 * Copyright (c) 2019-2022 Boris Timofeev <btimofeev@emunix.org>
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.databinding.FragmentInstalledGamesBinding
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import javax.inject.Inject

@AndroidEntryPoint
class InstalledGamesFragment : Fragment(R.layout.fragment_installed_games) {

    @Inject lateinit var gameManager: GameManager

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

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_installedGamesFragment_to_repositoryFragment)
        }

        binding.list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(binding.list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(binding.list.context, R.dimen.installed_game_fragment_inset_divider_margin_start)
        dividerItemDecoration.setDrawable(insetDivider)
        binding.list.addItemDecoration(dividerItemDecoration)
        listAdapter = InstalledGamesAdapter { viewModel.playGame(it.name) }
        listAdapter.setHasStableIds(true)
        binding.list.adapter = listAdapter
        binding.list.setHasFixedSize(true)
        registerForContextMenu(binding.list)

        viewModel.init()
        viewModel.getInstalledGames().observe(viewLifecycleOwner) { games ->
            listAdapter.submitList(games.toList())
            binding.emptyView.visible(games.isEmpty())
        }

        appArgumentViewModel.zipUri.observe(viewLifecycleOwner) { zipUri ->
            zipUri?.let {
                findNavController().navigate(R.id.action_installedGamesFragment_to_repositoryFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_installed_games, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_installedGamesFragment_to_settingsFragment)
                return true
            }
            R.id.action_about -> {
                findNavController().navigate(R.id.action_installedGamesFragment_to_aboutFragment)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        //super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.menu_context_installed_games, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val gameName = listAdapter.longClickedGame.name
        when (item.itemId) {
            R.id.installed_games_activity_context_menu_play -> {
                viewModel.playGame(gameName)
            }
            R.id.installed_games_activity_context_menu_play_from_beginning -> {
                viewModel.playGame(gameName, true)
            }
            R.id.installed_games_activity_context_menu_delete -> {
                val deleteDialog = DeleteGameDialog.newInstance(gameName, gameManager)
                if (isAdded)
                    parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
            }
            R.id.installed_games_activity_context_menu_about -> {
                val bundle = bundleOf("game_name" to gameName)
                findNavController().navigate(R.id.action_installedGamesFragment_to_gameFragment, bundle)
            }
        }
        return super.onContextItemSelected(item)
    }
}