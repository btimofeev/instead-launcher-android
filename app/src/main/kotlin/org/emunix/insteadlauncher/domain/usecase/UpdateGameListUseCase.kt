/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.model.UpdateGameListResult
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Error
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Success
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import javax.inject.Inject

interface UpdateGameListUseCase {

    suspend operator fun invoke() : UpdateGameListResult
}

class UpdateGameListUseCaseImpl @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val dataBaseRepository: DataBaseRepository,
    private val scanAndUpdateLocalGamesUseCase: ScanAndUpdateLocalGamesUseCase
) : UpdateGameListUseCase {

    override suspend fun invoke() : UpdateGameListResult {
        try {
            val games = remoteRepository.getGameList()
            dataBaseRepository.replaceAll(games)
            scanAndUpdateLocalGamesUseCase()
        } catch (e: Throwable) {
            return Error(e)
        }

        return Success
    }
}