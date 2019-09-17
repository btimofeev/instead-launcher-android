/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_installed_games.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.ui.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.ui.game.GameActivity
import org.emunix.insteadlauncher.ui.instead.InsteadActivity


class InstalledGamesFragment : Fragment() {

    private val listAdapter = InstalledGamesAdapter { playGame(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_installed_games, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(list.context, R.dimen.inset_divider_margin_start)
        dividerItemDecoration.setDrawable(insetDivider)
        list.addItemDecoration(dividerItemDecoration)
        listAdapter.setHasStableIds(true)
        list.adapter = listAdapter
        list.setHasFixedSize(true)
        registerForContextMenu(list)

        val viewModel = ViewModelProviders.of(this).get(InstalledGamesViewModel::class.java)

        viewModel.getInstalledGames().observe(this, Observer { games ->
            listAdapter.submitList(games)
            empty_view.visible(games.isEmpty())
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
                fragmentManager?.let { deleteDialog.show(it, "delete_dialog") }
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