package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_installed_games.*
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.ui.instead.InsteadActivity
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.services.UpdateResources
import org.emunix.insteadlauncher.ui.settings.SettingsActivity

class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_games)
        setSupportActionBar(toolbar)

        list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)

        InsteadLauncher.db.games().observeInstalledGames()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val games: List<Game> = it
                    if (games.isEmpty()) {
                        //todo show empty view
                    } else
                        list.adapter = InstalledGamesAdapter(games){
                            val intent = Intent(this, InsteadActivity::class.java)
                            val gameName = it.name
                            intent.putExtra("game_name", gameName)
                            startActivity(intent)
                        }
                }

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
}
