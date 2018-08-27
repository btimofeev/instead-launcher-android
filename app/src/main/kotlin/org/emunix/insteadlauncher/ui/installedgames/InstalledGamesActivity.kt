package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_installed_games.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.services.DeleteGame
import org.emunix.insteadlauncher.ui.instead.InsteadActivity
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.services.UpdateResources
import org.emunix.insteadlauncher.ui.game.GameActivity
import org.emunix.insteadlauncher.ui.settings.SettingsActivity


class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner {

    private val listAdapter = InstalledGamesAdapter { playGame(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_games)
        setSupportActionBar(toolbar)

        list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)
        list.adapter = listAdapter
        list.setHasFixedSize(true)
        registerForContextMenu(list)

        val viewModel = ViewModelProviders.of(this).get(InstalledGamesViewModel::class.java)

        viewModel.getInstalledGames().observe(this, Observer { games ->
            listAdapter.loadItems(games)
            listAdapter.notifyDataSetChanged()
        })

        fab.setOnClickListener { view -> startActivity(Intent(view.context, RepositoryActivity::class.java)) }

        UpdateResources.startActionUpdate(this, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_installed_games, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun playGame(game: Game, playFromBeginning: Boolean = false) {
        val intent = Intent(this, InsteadActivity::class.java)
        intent.putExtra("game_name", game.name)
        intent.putExtra("play_from_beginning", playFromBeginning)
        startActivity(intent)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_context_installed_games, menu)
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
                val dialog = AlertDialog.Builder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                dialog.setTitle(R.string.dialog_delete_game_title)
                dialog.setMessage(R.string.dialog_delete_game_text)
                dialog.setPositiveButton(R.string.dialog_delete_game_positive_button) { _, _ ->
                    val deleteGame = Intent(this, DeleteGame::class.java)
                    deleteGame.putExtra("game_name", listAdapter.getLongClickedGame().name)
                    startService(deleteGame)
                }
                dialog.setNegativeButton(R.string.dialog_delete_game_negative_button) { dialog, _ ->
                    dialog.cancel()
                }
                dialog.create()
                dialog.show()
            }
            R.id.installed_games_activity_context_menu_about -> {
                val intent = Intent(this, GameActivity::class.java)
                val gameName = listAdapter.getLongClickedGame().name
                intent.putExtra("game_name", gameName)
                startActivity(intent)
            }
        }
        return super.onContextItemSelected(item)
    }
}
