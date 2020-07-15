/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.network

import android.content.Context
import android.content.SharedPreferences
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.RxBus
import org.emunix.insteadlauncher.repository.fetcher.GameListFetcher
import org.emunix.insteadlauncher.repository.parser.GameListParser
import org.emunix.insteadlauncher.services.ScanGames
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class RepoUpdater(private val context: Context, private val fetcher: GameListFetcher,
                  private val parser: GameListParser, private val prefs: SharedPreferences) {

    fun update(): Boolean {
        RxBus.publish(UpdateRepoEvent(true))

        val games: ArrayList<Game> = arrayListOf()

        try {
            val gamesMap: MutableMap<String, Game> = mutableMapOf()
            if (isSandboxEnabled()){
                gamesMap.putAll(parseXML(fetchXML(getSandbox())))
            }
            gamesMap.putAll(parseXML(fetchXML(getRepo())))
            gamesMap.forEach { (_, value) -> games.add(value) }
        } catch (e: XmlPullParserException) {
            RxBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = false, isError = true,
                    message = context.getString(R.string.error_xml_parse, e.message)))
            return false
        } catch (e: IOException) {
            RxBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = false, isError = true,
                    message = context.getString(R.string.error_server_return_unexpected_code, e.message)))
            return false
        }

        InsteadLauncher.db.games().updateRepository(games)

        RxBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = true))

        ScanGames.start(context)
        return true
    }

    private fun fetchXML(url: String): String = fetcher.fetch(url)

    private fun parseXML(xml: String): Map<String, Game> = parser.parse(xml)

    private fun getRepo(): String = prefs.getString("pref_repository", InsteadLauncher.DEFAULT_REPOSITORY)!!

    private fun getSandbox(): String = prefs.getString("pref_sandbox", InsteadLauncher.SANDBOX)!!

    private fun isSandboxEnabled(): Boolean =  prefs.getBoolean("pref_sandbox_enabled", false)
}