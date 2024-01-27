/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState.IS_DELETE
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import java.io.IOException
import javax.inject.Inject

interface DeleteGameUseCase {

    @Throws(IOException::class)
    suspend operator fun invoke(gameName: String)
}

class DeleteGameUseCaseImpl @Inject constructor(
    private val fileSystemRepository: FileSystemRepository,
    private val dataBaseRepository: DataBaseRepository,
) : DeleteGameUseCase {

    override suspend fun invoke(gameName: String) {
        val game = dataBaseRepository.getGame(gameName)
        game.markToDeleteInDataBase()
        fileSystemRepository.deleteGameFromDisk(gameName)
        game.markAsNotInstalledInDataBase()
    }

    private suspend fun GameModel?.markToDeleteInDataBase() {
        this?.let { game ->
            dataBaseRepository.updateGame(game.copy(state = IS_DELETE))
        }
    }

    private suspend fun GameModel?.markAsNotInstalledInDataBase() {
        this?.let { game ->
            if (game.isInstalledFromSite) {
                dataBaseRepository.markAsNotInstalled(game)
            } else {
                dataBaseRepository.deleteGame(game.name)
            }
        }
    }
}