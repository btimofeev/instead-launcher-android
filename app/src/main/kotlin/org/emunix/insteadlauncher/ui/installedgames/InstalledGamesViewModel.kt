/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.services.UpdateRepositoryWork

class InstalledGamesViewModel(var app: Application): AndroidViewModel(app) {

    private val games = InsteadLauncher.db.games().observeInstalledGames()

    fun init() {
        startUpdateRepoWorker()
    }

    private fun startUpdateRepoWorker() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        val updatePref = sharedPreferences.getBoolean("pref_update_repo_background", true)

        if (updatePref) {
            UpdateRepositoryWork.start(app)
        } else {
            UpdateRepositoryWork.stop(app)
        }
    }

    fun getInstalledGames(): LiveData<List<Game>> = games
}
