/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.services.UpdateRepositoryWorkManager
import javax.inject.Inject

@HiltViewModel
class InstalledGamesViewModel @Inject constructor(
    private val gameManager: GameManager,
    private val gamesDB: GameDao,
    private val preferencesProvider: PreferencesProvider,
    private val updateRepositoryWorkManager: UpdateRepositoryWorkManager,
) : ViewModel() {

    private val games = gamesDB.observeInstalledGames()

    fun init() {
        startUpdateRepoWorker()
    }

    private fun startUpdateRepoWorker() {
        val updatePref = preferencesProvider.updateRepoInBackground

        if (updatePref) {
            updateRepositoryWorkManager.start()
        } else {
            updateRepositoryWorkManager.stop()
        }
    }

    fun getInstalledGames(): LiveData<List<Game>> = games

    fun playGame(gameName: String, playFromBeginning: Boolean = false) {
        gameManager.startGame(gameName, playFromBeginning)
    }
}
