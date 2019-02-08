package org.emunix.insteadlauncher.ui.installedgames

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_installed_games.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.services.UpdateResources
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.ui.settings.SettingsActivity


class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_games)
        setSupportActionBar(toolbar)

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
