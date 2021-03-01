/*
 * Copyright (c) 2019-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.databinding.FragmentInstalledGamesBinding
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.ui.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.ui.game.GameActivity
import org.emunix.insteadlauncher.ui.instead.InsteadActivity


class InstalledGamesFragment : Fragment() {

    private var _binding: FragmentInstalledGamesBinding? = null
    private val binding get() = _binding!!

    private val listAdapter = InstalledGamesAdapter { playGame(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentInstalledGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(binding.list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(binding.list.context, R.dimen.installed_game_fragment_inset_divider_margin_start)
        dividerItemDecoration.setDrawable(insetDivider)
        binding.list.addItemDecoration(dividerItemDecoration)
        listAdapter.setHasStableIds(true)
        binding.list.adapter = listAdapter
        binding.list.setHasFixedSize(true)
        registerForContextMenu(binding.list)

        val viewModel = ViewModelProvider(this).get(InstalledGamesViewModel::class.java)
        viewModel.init()

        viewModel.getInstalledGames().observe(viewLifecycleOwner, Observer { games ->
            listAdapter.submitList(games)
            binding.emptyView.visible(games.isEmpty())
        })
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        //super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.menu_context_installed_games, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.installed_games_activity_context_menu_play -> {
                playGame(listAdapter.getLongClickedGame())
            }
            R.id.installed_games_activity_context_menu_play_from_beginning -> {
                playGame(listAdapter.getLongClickedGame(), true)
            }
            R.id.installed_games_activity_context_menu_delete -> {
                val deleteDialog = DeleteGameDialog.newInstance(listAdapter.getLongClickedGame().name)
                if (isAdded)
                    parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
            }
            R.id.installed_games_activity_context_menu_about -> {
                val intent = Intent(context, GameActivity::class.java)
                val gameName = listAdapter.getLongClickedGame().name
                intent.putExtra("game_name", gameName)
                startActivity(intent)
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