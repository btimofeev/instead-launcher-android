/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.domain.ActiveDownloadsRegistry
import org.emunix.insteadlauncher.domain.model.GameState.IN_QUEUE_TO_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.model.GameState.IS_DELETE
import org.emunix.insteadlauncher.domain.model.GameState.IS_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.IS_UPDATE
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import java.io.File
import javax.inject.Inject

interface RecoverInterruptedOperationsUseCase {

    suspend operator fun invoke(): Boolean
}

class RecoverInterruptedOperationsUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository,
    private val fileSystemRepository: FileSystemRepository,
    private val storage: Storage,
    private val activeDownloadsRegistry: ActiveDownloadsRegistry,
) : RecoverInterruptedOperationsUseCase {

    override suspend fun invoke(): Boolean {
        val activeGameNames = activeDownloadsRegistry.activeGameNames()
        val stuckGames = dataBaseRepository.getStuckGames()
            .filterNot { it.name in activeGameNames }
        if (stuckGames.isEmpty()) {
            return false
        }
        var recovered = false
        stuckGames.forEach { game ->
            when (game.state) {
                IS_DELETE -> {
                    fileSystemRepository.deleteGameFromDisk(game.name)
                    if (game.isInstalledFromSite) {
                        dataBaseRepository.markAsNotInstalled(game)
                    } else {
                        dataBaseRepository.deleteGame(game.name)
                    }
                    recovered = true
                }
                IN_QUEUE_TO_INSTALL, IS_INSTALL, IS_UPDATE -> {
                    fileSystemRepository.cleanupGameTempFiles(game.name)
                    if (File(storage.getGamesDirectory(), game.name).exists()) {
                        dataBaseRepository.updateGame(game.copy(state = INSTALLED))
                    } else {
                        dataBaseRepository.markAsNotInstalled(game)
                    }
                    recovered = true
                }
                else -> Unit
            }
        }
        return recovered
    }
}