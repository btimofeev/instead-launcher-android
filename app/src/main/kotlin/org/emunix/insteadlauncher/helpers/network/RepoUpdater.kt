/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.insteadlauncher.data.model.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.helpers.resourceprovider.ResourceProvider
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.data.fetcher.GameListFetcher
import org.emunix.insteadlauncher.data.parser.GameListParser
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.inject.Inject

class RepoUpdater @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fetcher: GameListFetcher,
    private val parser: GameListParser,
    private val eventBus: EventBus,
    private val dataBaseRepository: DataBaseRepository,
    private val gameManager: GameManager,
    private val preferencesProvider: PreferencesProvider
) {

    suspend fun update(): Boolean = withContext(Dispatchers.IO) {
        eventBus.publish(UpdateRepoEvent(true))

        val games = mutableListOf<GameModel>()

        try {
            val gamesMap = mutableMapOf<String, GameModel>()
            if (preferencesProvider.isSandboxEnabled) {
                gamesMap.putAll(parseXML(fetchXML(preferencesProvider.sandboxUrl)))
            }
            gamesMap.putAll(parseXML(fetchXML(preferencesProvider.repositoryUrl)))
            gamesMap.forEach { (_, value) -> games.add(value) }
        } catch (e: XmlPullParserException) {
            eventBus.publish(
                UpdateRepoEvent(
                    isLoading = false, isGamesLoaded = false, isError = true,
                    message = resourceProvider.getString(string.error_xml_parse, e.message.orEmpty())
                )
            )
            return@withContext false
        } catch (e: IOException) {
            eventBus.publish(
                UpdateRepoEvent(
                    isLoading = false, isGamesLoaded = false, isError = true,
                    message = resourceProvider.getString(
                        string.error_server_return_unexpected_code,
                        e.message.orEmpty()
                    )
                )
            )
            return@withContext false
        }

        dataBaseRepository.replaceAll(games)

        eventBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = true))

        gameManager.scanGames()
        return@withContext true
    }

    private fun fetchXML(url: String): String = fetcher.fetch(url)

    private fun parseXML(xml: String): Map<String, GameModel> = parser.parse(xml)
}