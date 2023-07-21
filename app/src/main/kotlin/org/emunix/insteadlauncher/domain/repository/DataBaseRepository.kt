/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import org.emunix.insteadlauncher.domain.model.GameModel

interface DataBaseRepository {

    suspend fun addOrReplaceGame(game: GameModel)

    suspend fun updateGame(game: GameModel)

    suspend fun deleteGame(name: String)

    suspend fun getGame(name: String): GameModel?

    suspend fun getInstalledGames(): List<GameModel>

    suspend fun markAsNotInstalled(game: GameModel)
}