package org.emunix.insteadlauncher.ui.installedgames

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_installed_games.*
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.ui.repository.RepositoryView


class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner, RepositoryView {

    private val mRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = mRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_games)
        setSupportActionBar(toolbar)

        list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(list.context, LinearLayout.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)

        InsteadLauncher.gamesDB.gameDao().getInstalledGames()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val games: List<Game> = it
                    if (games.isEmpty()) {
                        //todo show empty view
                    } else
                        list.adapter = InstalledGamesAdapter(games){
                            showError("Представь, что игра запустилась")
//                            val intent = Intent(this, GameActivity::class.java)
//                            val gameName = it.name
//                            intent.putExtra("game_name", gameName)
//                            startActivity(intent)
                        }
                })

        fab.setOnClickListener { view -> startActivity(Intent(view.context, RepositoryActivity::class.java)) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_installed_games, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun setLoadingIndicator(isActive: Boolean) {

    }

    override fun showGames(games: List<Game>) {

    }

    override fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun showEmptyView(isActive: Boolean) {

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
