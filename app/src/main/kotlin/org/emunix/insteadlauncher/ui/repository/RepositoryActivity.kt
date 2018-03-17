package org.emunix.insteadlauncher.ui.repository

import android.arch.lifecycle.LifecycleOwner
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import kotlinx.android.synthetic.main.activity_repository.*
import android.content.Intent
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.services.UpdateRepository
import android.arch.lifecycle.LifecycleRegistry
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.support.v7.widget.DividerItemDecoration
import org.emunix.insteadlauncher.ui.game.GameActivity


class RepositoryActivity: AppCompatActivity(), LifecycleOwner, RepositoryView {

    private val mRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = mRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = getString(R.string.repository_activity_title)

        list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)

        InsteadLauncher.gamesDB.gameDao().getAll()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val games: List<Game> = it
                    if (games.isEmpty()) {
                        val updateRepoIntent = Intent(this, UpdateRepository::class.java)
                        startService(updateRepoIntent)
                    } else
                        list.adapter = RepositoryAdapter(games){
                            val intent = Intent(this, GameActivity::class.java)
                            val gameName = it.name
                            intent.putExtra("game_name", gameName)
                            startActivity(intent)
                        }
                })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repository, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_update_repo -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setLoadingIndicator(isActive: Boolean) {
        progress.visibility = if (isActive) View.VISIBLE else View.GONE
    }

    override fun showGames(games: List<Game>) {
        for (game in games) {
            Log.d("Instead", game.title)
        }
    }

    override fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun showEmptyView(isActive: Boolean) {
        empty.visibility = if (isActive) View.VISIBLE else View.GONE
        list.visibility = if (isActive) View.GONE else View.VISIBLE
    }

}
