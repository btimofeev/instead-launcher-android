/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import javax.inject.Inject

interface GetGamesFlowUseCase {

    suspend operator fun invoke(onlyInstalled: Boolean = false): Flow<List<GameModel>>
}

class GetGamesFlowUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository
) : GetGamesFlowUseCase {

    override suspend fun invoke(onlyInstalled: Boolean): Flow<List<GameModel>> {
        return dataBaseRepository.observeGames()
            .map { games ->
                if (onlyInstalled) {
                    games.filter { it.state == GameState.INSTALLED }
                } else {
                    games
                }
            }
    }
}