/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.network

import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.insteadlauncher.data.model.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.helpers.resourceprovider.ResourceProvider
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.data.fetcher.GameListFetcher
import org.emunix.insteadlauncher.data.parser.GameListParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.inject.Inject

class RepoUpdater @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fetcher: GameListFetcher,
    private val parser: GameListParser,
    private val eventBus: EventBus,
    private val gamesDB: GameDao,
    private val gameManager: GameManager,
    private val preferencesProvider: PreferencesProvider
) {

    fun update(): Boolean {
        eventBus.publish(UpdateRepoEvent(true))

        val games: ArrayList<Game> = arrayListOf()

        try {
            val gamesMap: MutableMap<String, Game> = mutableMapOf()
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
            return false
        } catch (e: IOException) {
            try {
                val gamesMap: MutableMap<String, Game> = mutableMapOf()
                if (preferencesProvider.isSandboxEnabled) {
                    val fallbackSandbox = preferencesProvider.sandboxUrl.replace("https://" , "http://")
                    gamesMap.putAll(parseXML(fetchXML(fallbackSandbox)))
                }
                val fallbackRepo = preferencesProvider.repositoryUrl.replace("https://" , "http://")
                gamesMap.putAll(parseXML(fetchXML(fallbackRepo)))
                gamesMap.forEach { (_, value) -> games.add(value) }
            } catch (e: XmlPullParserException) {
                eventBus.publish(
                    UpdateRepoEvent(
                        isLoading = false, isGamesLoaded = false, isError = true,
                        message = resourceProvider.getString(string.error_xml_parse, e.message.orEmpty())
                    )
                )
                return false
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
                return false
            }
        }

        gamesDB.updateRepository(games)

        eventBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = true))

        gameManager.scanGames()
        return true
    }

    private fun fetchXML(url: String): String = fetcher.fetch(url)

    private fun parseXML(xml: String): Map<String, Game> = parser.parse(xml)
}