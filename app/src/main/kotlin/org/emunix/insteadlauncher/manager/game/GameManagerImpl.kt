/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.manager.game

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead_api.InsteadApi
import org.emunix.insteadlauncher.helpers.gameparser.GameParser
import org.emunix.insteadlauncher.helpers.gameparser.NotInsteadGameZipException
import org.emunix.insteadlauncher.helpers.unzip
import org.emunix.insteadlauncher.services.DeleteGame
import org.emunix.insteadlauncher.services.InstallGame
import org.emunix.insteadlauncher.services.ScanGames
import java.io.IOException
import javax.inject.Inject

class GameManagerImpl @Inject constructor(
    private val context: Context,
    private val insteadApi: InsteadApi,
    private val gameParser: GameParser,
    private val storage: Storage
) : GameManager {

    override fun startGame(gameName: String, playFromBeginning: Boolean) {
        insteadApi.startGame(gameName, playFromBeginning)
    }

    override fun installGame(gameName: String, gameUrl: String, gameTitle: String) {
        InstallGame.start(context, gameName, gameUrl, gameTitle)
    }

    override fun deleteGame(gameName: String) {
        DeleteGame.start(context, gameName)
    }

    override fun scanGames() {
        ScanGames.start(context)
    }

    override suspend fun installGameFromZip(uri: Uri) {

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
            } else
                throw NotInsteadGameZipException("main.lua not found")
        }
    }
}