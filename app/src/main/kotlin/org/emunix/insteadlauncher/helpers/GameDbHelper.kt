/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.GameDao
import javax.inject.Inject

class GameDbHelper @Inject constructor(private val gamesDao: GameDao) {

    fun saveStateToDB(game: Game, state: Game.State) {
        game.state = state
        gamesDao.update(game)
    }

    fun saveInstalledVersionToDB(game: Game, version: String) {
        game.installedVersion = version
        gamesDao.update(game)
    }
}