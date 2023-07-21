/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.insteadlauncher.data.mapper.toData
import org.emunix.insteadlauncher.data.mapper.toDomain
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState.NO_INSTALLED
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import javax.inject.Inject

class DataBaseRepositoryImpl @Inject constructor(
    private val gameDao: GameDao,
) : DataBaseRepository {

    override suspend fun addOrReplaceGame(game: GameModel) = withContext(Dispatchers.IO) {
        gameDao.insert(game.toData())
    }

    override suspend fun replaceAll(games: List<GameModel>) = withContext(Dispatchers.IO) {
        gameDao.updateRepository(games.map { it.toData() })
    }

    override suspend fun updateGame(game: GameModel) = withContext(Dispatchers.IO) {
        gameDao.update(game.toData())
    }

    override suspend fun deleteGame(name: String) = withContext(Dispatchers.IO) {
        gameDao.deleteByName(name)
    }

    override suspend fun getGame(name: String): GameModel? = withContext(Dispatchers.IO) {
        return@withContext try {
            gameDao.getByName(name).toDomain()
        } catch (e: Throwable) {
            null
        }
    }

    override suspend fun getInstalledGames(): List<GameModel> = withContext(Dispatchers.IO) {
        gameDao.getInstalledGames().map { it.toDomain() }
    }

    override suspend fun markAsNotInstalled(game: GameModel) = withContext(Dispatchers.IO) {
        gameDao.update(
            game.copy(
                state = NO_INSTALLED,
                version = game.version.copy(installed = "")
            ).toData()
        )
    }
}