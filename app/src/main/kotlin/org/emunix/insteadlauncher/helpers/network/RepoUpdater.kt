/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.network

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.InsteadGamesXMLParser
import org.emunix.insteadlauncher.helpers.RxBus
import org.emunix.insteadlauncher.services.ScanGames
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class RepoUpdater(val context: Context) {

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

    @Throws (IOException::class)
    private fun fetchXML(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("${response.code}")
        return response.body!!.string()
    }

    private fun parseXML(xml: String): Map<String, Game> = InsteadGamesXMLParser().parse(xml)

    private fun getRepo(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("pref_repository", InsteadLauncher.DEFAULT_REPOSITORY)!!
    }

    private fun getSandbox(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("pref_sandbox", InsteadLauncher.SANDBOX)!!
    }

    private fun isSandboxEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("pref_sandbox_enabled", false)
    }
}