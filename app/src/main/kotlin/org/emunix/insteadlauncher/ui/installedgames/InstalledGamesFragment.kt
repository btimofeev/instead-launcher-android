package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_installed_games.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.services.DeleteGame
import org.emunix.insteadlauncher.ui.game.GameActivity
import org.emunix.insteadlauncher.ui.instead.InsteadActivity

class InstalledGamesFragment : Fragment() {

    private val listAdapter = InstalledGamesAdapter { playGame(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_installed_games, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)
        list.adapter = listAdapter
        list.setHasFixedSize(true)
        registerForContextMenu(list)

        val viewModel = ViewModelProviders.of(this).get(InstalledGamesViewModel::class.java)

        viewModel.getInstalledGames().observe(this, Observer { games ->
            listAdapter.loadItems(games)
            listAdapter.notifyDataSetChanged()
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
                val dialog = AlertDialog.Builder(activity!!, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                dialog.setTitle(R.string.dialog_delete_game_title)
                dialog.setMessage(R.string.dialog_delete_game_text)
                dialog.setPositiveButton(R.string.dialog_delete_game_positive_button) { _, _ ->
                    val deleteGame = Intent(activity, DeleteGame::class.java)
                    deleteGame.putExtra("game_name", listAdapter.getLongClickedGame().name)
                    activity?.startService(deleteGame)
                }
                dialog.setNegativeButton(R.string.dialog_delete_game_negative_button) { dialog, _ ->
                    dialog.cancel()
                }
                dialog.create()
                dialog.show()
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