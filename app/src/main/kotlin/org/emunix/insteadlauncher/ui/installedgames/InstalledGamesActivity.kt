/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.databinding.ActivityInstalledGamesBinding
import org.emunix.insteadlauncher.services.ScanGames
import org.emunix.insteadlauncher.ui.about.AboutActivity
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import org.emunix.insteadlauncher.ui.settings.SettingsActivity


class InstalledGamesActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInstalledGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            startActivity(
                    Intent(view.context, RepositoryActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }

        val unpackViewModel = ViewModelProvider(this).get(UnpackResourcesViewModel::class.java)
        unpackViewModel.getUnpackSuccessStatus().observe(this) { isSuccess ->
            if (isSuccess) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, InstalledGamesFragment())
                        .commit()
            }
        }

        ScanGames.start(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_installed_games, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
