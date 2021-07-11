/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.GameDao
import org.emunix.insteadlauncher.interactor.GamesInteractor
import org.emunix.insteadlauncher.services.UpdateRepositoryWork
import javax.inject.Inject

@HiltViewModel
class InstalledGamesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gamesInteractor: GamesInteractor,
    private val gamesDB: GameDao,
    private val preferences: SharedPreferences
) : ViewModel() {

    private val games = gamesDB.observeInstalledGames()

    fun init() {
        startUpdateRepoWorker()
    }

    private fun startUpdateRepoWorker() {
        val updatePref = preferences.getBoolean("pref_update_repo_background", true)

        if (updatePref) {
            UpdateRepositoryWork.start(context)
        } else {
            UpdateRepositoryWork.stop(context)
        }
    }

    fun getInstalledGames(): LiveData<List<Game>> = games

    fun playGame(gameName: String, playFromBeginning: Boolean = false) {
        gamesInteractor.startGame(gameName, playFromBeginning)
    }
}
