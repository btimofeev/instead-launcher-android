package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_installed_games.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.ui.instead.InsteadActivity
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.services.UpdateResources
import org.emunix.insteadlauncher.ui.settings.SettingsActivity

class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var viewModel: InstalledGamesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_games)
        setSupportActionBar(toolbar)

        list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProviders.of(this).get(InstalledGamesViewModel::class.java)

        viewModel.getInstalledGames().observe(this, Observer { games ->
            updateList(games)
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

    private fun updateList(games: List<Game>) {
            list.adapter = InstalledGamesAdapter(games){
                val intent = Intent(this, InsteadActivity::class.java)
                val gameName = it.name
                intent.putExtra("game_name", gameName)
                startActivity(intent)
            }
    }
}
