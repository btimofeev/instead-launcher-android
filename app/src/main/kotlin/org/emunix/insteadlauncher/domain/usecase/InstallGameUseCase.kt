/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.model.GameState.IS_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.NO_INSTALLED
import org.emunix.insteadlauncher.domain.model.InstallGameResult
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.DOWNLOAD_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.GAME_NOT_FOUND_IN_DATABASE
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.UNPACKING_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Success
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import javax.inject.Inject

interface InstallGameUseCase {

    suspend operator fun invoke(gameName: String): InstallGameResult
}

class InstallGameUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository,
    private val remoteRepository: RemoteRepository,
    private val fileSystemRepository: FileSystemRepository,
) : InstallGameUseCase {

    override suspend fun invoke(gameName: String): InstallGameResult {
        val game = dataBaseRepository.getGame(gameName) ?: return Error(type = GAME_NOT_FOUND_IN_DATABASE)
        val url = game.url.download
        game.saveInstallStateToDatabase()

        val stream = try {
            remoteRepository.download(url, gameName)
        } catch (e: Throwable) {
            game.saveNotInstalledStateToDatabase()
            return Error(type = DOWNLOAD_ERROR, throwable = e)
        }

        try {
            fileSystemRepository.installGame(gameName, stream)
        } catch (e: Throwable) {
            game.saveNotInstalledStateToDatabase()
            return Error(type = UNPACKING_ERROR, throwable = e)
        }

        game.saveInstalledVersionToDatabase(game.version.availableOnSite)
        return Success
    }

    private suspend fun GameModel.saveInstallStateToDatabase() =
        dataBaseRepository.updateGame(this.copy(state = IS_INSTALL))

    private suspend fun GameModel.saveNotInstalledStateToDatabase() =
        dataBaseRepository.updateGame(this.copy(state = NO_INSTALLED))

    private suspend fun GameModel.saveInstalledVersionToDatabase(newVersion: String) =
        dataBaseRepository.updateGame(
            this.copy(
                state = INSTALLED,
                version = this.version.copy(installed = newVersion)
            )
        )
}
