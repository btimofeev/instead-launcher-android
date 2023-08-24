/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import javax.inject.Inject

interface SearchGamesUseCase {

    suspend operator fun invoke(query: String) : List<GameModel>
}

class SearchGamesUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository,
) : SearchGamesUseCase {

    override suspend fun invoke(query: String): List<GameModel> {
        return dataBaseRepository.search(query)
    }
}