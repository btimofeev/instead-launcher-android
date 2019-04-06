/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

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
import org.emunix.insteadlauncher.ui.about.AboutActivity
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.ui.settings.SettingsActivity


class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_games)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view -> startActivity(Intent(view.context, RepositoryActivity::class.java)) }

        UpdateResources.start(this, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_installed_games, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
