/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.domain.model.GameInfo
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.model.GameUrl
import org.emunix.insteadlauncher.domain.model.GameVersion
import org.emunix.insteadlauncher.domain.parser.GameParser
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.utils.writeToLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

interface ScanAndUpdateLocalGamesUseCase {

    suspend operator fun invoke()
}

class ScanAndUpdateLocalGamesUseCaseImpl @Inject constructor(
    private val dataBaseRepository: DataBaseRepository,
    private val storage: Storage,
    private val gameParser: GameParser,
): ScanAndUpdateLocalGamesUseCase {

    override suspend fun invoke() {
        removeDeletedGamesFromDataBase()
        scanAndAddNewGamesToDataBase()
    }

    private suspend fun removeDeletedGamesFromDataBase() = withContext(Dispatchers.IO) {
        getGameNamesToRemoveFromDataBase().forEach { name ->
            dataBaseRepository.getGame(name)?.let { game ->
                if (game.isInstalledFromSite) {
                    dataBaseRepository.markAsNotInstalled(game)
                } else {
                    dataBaseRepository.deleteGame(game.name)
                }
            }
        }
    }

    private suspend fun getGameNamesToRemoveFromDataBase(): List<String> {
        val installedNamesInDatabase = dataBaseRepository.getInstalledGames()
            .ifEmpty { return emptyList() }
            .map { it.name }

        val localNames = storage.getGamesDirectory().list() ?: return emptyList()

        return installedNamesInDatabase.filterNot { localNames.contains(it) }
    }

    private suspend fun scanAndAddNewGamesToDataBase() {
        val installedNames = dataBaseRepository.getInstalledGames().map { it.name }
        val gamesDir = storage.getGamesDirectory()
        val localNames = gamesDir.list()
        if (localNames == null || localNames.isEmpty()) {
            return
        }

        val newNames = localNames.filterNot { installedNames.contains(it) }
        newNames.forEach { gameName ->
            val gameDir = File(gamesDir, gameName)
            if (gameParser.isInsteadGame(gameDir)) {
                addGameToDataBase(gameDir, gameName)
            }
        }
    }

    private suspend fun addGameToDataBase(gameDir: File, gameName: String) {
        try {
            val lang = Locale.getDefault().language
            val file = gameParser.getMainGameFile(gameDir)
            val version = gameParser.getVersion(file, lang)

            dataBaseRepository.addOrReplaceGame(
                GameModel(
                    name = gameName,
                    info = GameInfo(
                        title = gameParser.getTitle(file, lang),
                        author = gameParser.getAuthor(file, lang),
                        description = gameParser.getInfo(file, lang),
                        lastReleaseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        gameSize = FileUtils.sizeOfDirectory(gameDir),
                    ),
                    url = GameUrl(),
                    version = GameVersion(
                        installed = version,
                        availableOnSite = version,
                    ),
                    state = INSTALLED
                )
            )
        } catch (e: Throwable) {
            e.writeToLog()
        }
    }
}