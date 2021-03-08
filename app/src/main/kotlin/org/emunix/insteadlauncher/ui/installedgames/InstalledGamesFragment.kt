/*
 * Copyright (c) 2019-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.*
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
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.databinding.FragmentInstalledGamesBinding
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.ui.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.ui.instead.InsteadActivity
import org.emunix.insteadlauncher.ui.launcher.AppArgumentViewModel


class InstalledGamesFragment : Fragment() {

    private var _binding: FragmentInstalledGamesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InstalledGamesViewModel by viewModels()
    private val appArgumentViewModel: AppArgumentViewModel by activityViewModels()

    private lateinit var listAdapter: InstalledGamesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentInstalledGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        listAdapter = InstalledGamesAdapter { playGame(it) }
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
        when (item.itemId) {
            R.id.installed_games_activity_context_menu_play -> {
                playGame(listAdapter.longClickedGame)
            }
            R.id.installed_games_activity_context_menu_play_from_beginning -> {
                playGame(listAdapter.longClickedGame, true)
            }
            R.id.installed_games_activity_context_menu_delete -> {
                val deleteDialog = DeleteGameDialog.newInstance(listAdapter.longClickedGame.name)
                if (isAdded)
                    parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
            }
            R.id.installed_games_activity_context_menu_about -> {
                val bundle = bundleOf("game_name" to listAdapter.longClickedGame.name)
                findNavController().navigate(R.id.action_installedGamesFragment_to_gameFragment, bundle)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun playGame(game: Game, playFromBeginning: Boolean = false) {
        val intent = Intent(activity, InsteadActivity::class.java)
        intent.putExtra("game_name", game.name)
        intent.putExtra("play_from_beginning", playFromBeginning)
        startActivity(intent)
    }
}