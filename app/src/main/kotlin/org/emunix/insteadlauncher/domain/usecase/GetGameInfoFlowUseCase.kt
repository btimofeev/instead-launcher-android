/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import javax.inject.Inject

interface GetGameInfoFlowUseCase {

    suspend operator fun invoke(gameName: String): Flow<GameModel?>
}

class GetGameInfoFlowUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository
) : GetGameInfoFlowUseCase {

    override suspend fun invoke(gameName: String): Flow<GameModel?> {
        return dataBaseRepository.observeGameByName(gameName)
    }
}