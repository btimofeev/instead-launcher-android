/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.model.GameState.IS_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.NO_INSTALLED
import org.emunix.insteadlauncher.domain.model.InstallGameResult
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.DOWNLOAD_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.GAME_NOT_FOUND_IN_DATABASE
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.INVALID_GAME_FILE
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.UNPACKING_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Success
import org.emunix.insteadlauncher.domain.model.InvalidGameFileException
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import javax.inject.Inject

interface InstallGameUseCase {

    suspend operator fun invoke(gameName: String, originalState: GameState): InstallGameResult
}

class InstallGameUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository,
    private val remoteRepository: RemoteRepository,
    private val fileSystemRepository: FileSystemRepository,
) : InstallGameUseCase {

override suspend fun invoke(gameName: String, originalState: GameState): InstallGameResult {
        val game = dataBaseRepository.getGame(gameName) ?: return Error(type = GAME_NOT_FOUND_IN_DATABASE)
        val url = game.url.download
        game.saveInstallStateToDatabase()

        val stream = try {
            remoteRepository.download(url, gameName)
        } catch (e: CancellationException) {
            game.restoreStateToDatabase(originalState)
            throw e
        } catch (e: InvalidGameFileException) {
            game.restoreStateToDatabase(originalState)
            return Error(type = INVALID_GAME_FILE, throwable = e)
        } catch (e: Throwable) {
            game.restoreStateToDatabase(originalState)
            return Error(type = DOWNLOAD_ERROR, throwable = e)
        }

        try {
            fileSystemRepository.installGame(gameName, stream)
        } catch (e: CancellationException) {
            deletePartialInstall(gameName)
            game.saveNotInstalledStateToDatabase()
            throw e
        } catch (e: Throwable) {
            deletePartialInstall(gameName)
            game.saveNotInstalledStateToDatabase()
            return Error(type = UNPACKING_ERROR, throwable = e)
        }

        game.saveInstalledVersionToDatabase(game.version.availableOnSite)
        return Success
    }

    private suspend fun GameModel.saveInstallStateToDatabase() =
        dataBaseRepository.updateGame(this.copy(state = IS_INSTALL))

    private suspend fun GameModel.restoreStateToDatabase(originalState: GameState) {
        withContext(NonCancellable) {
            dataBaseRepository.updateGame(this@restoreStateToDatabase.copy(state = originalState))
        }
    }

    private suspend fun deletePartialInstall(gameName: String) =
        withContext(NonCancellable) {
            fileSystemRepository.deleteGameFromDisk(gameName)
        }

    private suspend fun GameModel.saveNotInstalledStateToDatabase() {
        val notInstalledModel = copy(state = NO_INSTALLED)
        withContext(NonCancellable) {
            dataBaseRepository.updateGame(notInstalledModel)
        }
    }

    private suspend fun GameModel.saveInstalledVersionToDatabase(newVersion: String) =
        dataBaseRepository.updateGame(
            this.copy(
                state = INSTALLED,
                version = this.version.copy(installed = newVersion)
            )
        )
}
