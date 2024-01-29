/*
 * Copyright (c) 2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.manager.game

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead_api.InsteadApi
import org.emunix.insteadlauncher.domain.model.GameState.IN_QUEUE_TO_INSTALL
import org.emunix.insteadlauncher.domain.model.NotInsteadGameZipException
import org.emunix.insteadlauncher.domain.parser.GameParser
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.work.DeleteGameWork
import org.emunix.insteadlauncher.domain.work.ScanGamesWork
import org.emunix.insteadlauncher.services.InstallGame
import org.emunix.insteadlauncher.utils.unzip
import java.io.IOException
import javax.inject.Inject

class GameManagerImpl @Inject constructor(
    private val context: Context,
    private val insteadApi: InsteadApi,
    private val gameParser: GameParser,
    private val storage: Storage,
    private val dataBaseRepository: DataBaseRepository,
    private val deleteGameWork: DeleteGameWork,
    private val scanGamesWork: ScanGamesWork,
) : GameManager {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun startGame(gameName: String, playFromBeginning: Boolean) {
        insteadApi.startGame(gameName, playFromBeginning)
    }

    override fun installGame(gameName: String, gameUrl: String, gameTitle: String) {
        coroutineScope.launch {
            dataBaseRepository.getGame(gameName)?.let { game ->
                dataBaseRepository.updateGame(game.copy(state = IN_QUEUE_TO_INSTALL))
            }
        }
        InstallGame.start(context, gameName, gameUrl, gameTitle)
    }

    override fun deleteGame(gameName: String) {
        deleteGameWork.delete(gameName)
    }

    override fun scanGames() {
        scanGamesWork.scan()
    }

    override suspend fun installGameFromZip(uri: Uri) { // todo перенести код метода в usecase

        fun isGameZip(uri: Uri): Boolean {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("inputStream is null")
            val isInsteadGameZip = gameParser.isInsteadGameZip(inputStream)
            inputStream.close()
            return isInsteadGameZip
        }

        fun unzip(uri: Uri) {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("inputStream is null")
            val gamesDir = storage.getGamesDirectory()
            inputStream.unzip(gamesDir)
            inputStream.close()
        }

        withContext(Dispatchers.IO) {
            if (isGameZip(uri)) {
                unzip(uri)
            } else {
                throw NotInsteadGameZipException("main.lua not found")
            }
        }
    }
}