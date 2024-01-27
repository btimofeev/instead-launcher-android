/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import kotlinx.coroutines.flow.Flow
import org.emunix.insteadlauncher.domain.model.GameModel

interface DataBaseRepository {

    suspend fun addOrReplaceGame(game: GameModel)

    suspend fun replaceAll(games: List<GameModel>)

    suspend fun updateGame(game: GameModel)

    suspend fun deleteGame(name: String)

    suspend fun getGame(name: String): GameModel?

    suspend fun getInstalledGames(): List<GameModel>

    suspend fun observeGames(): Flow<List<GameModel>>

    suspend fun observeGameByName(name: String): Flow<GameModel?>

    suspend fun search(query: String): List<GameModel>

    suspend fun markAsNotInstalled(game: GameModel)
}