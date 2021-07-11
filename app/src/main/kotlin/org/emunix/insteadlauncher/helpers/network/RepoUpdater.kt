/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.network

import android.content.SharedPreferences
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.GameDao
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.helpers.resourceprovider.ResourceProvider
import org.emunix.insteadlauncher.interactor.GamesInteractor
import org.emunix.insteadlauncher.repository.fetcher.GameListFetcher
import org.emunix.insteadlauncher.repository.parser.GameListParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.inject.Inject

class RepoUpdater @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fetcher: GameListFetcher,
    private val parser: GameListParser,
    private val prefs: SharedPreferences,
    private val eventBus: EventBus,
    private val gamesDB: GameDao,
    private val gamesInteractor: GamesInteractor
) {

    fun update(): Boolean {
        eventBus.publish(UpdateRepoEvent(true))

        val games: ArrayList<Game> = arrayListOf()

        try {
            val gamesMap: MutableMap<String, Game> = mutableMapOf()
            if (isSandboxEnabled()) {
                gamesMap.putAll(parseXML(fetchXML(getSandbox())))
            }
            gamesMap.putAll(parseXML(fetchXML(getRepo())))
            gamesMap.forEach { (_, value) -> games.add(value) }
        } catch (e: XmlPullParserException) {
            eventBus.publish(
                UpdateRepoEvent(
                    isLoading = false, isGamesLoaded = false, isError = true,
                    message = resourceProvider.getString(R.string.error_xml_parse, e.message.orEmpty())
                )
            )
            return false
        } catch (e: IOException) {
            eventBus.publish(
                UpdateRepoEvent(
                    isLoading = false, isGamesLoaded = false, isError = true,
                    message = resourceProvider.getString(
                        R.string.error_server_return_unexpected_code,
                        e.message.orEmpty()
                    )
                )
            )
            return false
        }

        gamesDB.updateRepository(games)

        eventBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = true))

        gamesInteractor.scanGames()
        return true
    }

    private fun fetchXML(url: String): String = fetcher.fetch(url)

    private fun parseXML(xml: String): Map<String, Game> = parser.parse(xml)

    private fun getRepo(): String = prefs.getString("pref_repository", InsteadLauncher.DEFAULT_REPOSITORY)!!

    private fun getSandbox(): String = prefs.getString("pref_sandbox", InsteadLauncher.SANDBOX)!!

    private fun isSandboxEnabled(): Boolean = prefs.getBoolean("pref_sandbox_enabled", false)
}