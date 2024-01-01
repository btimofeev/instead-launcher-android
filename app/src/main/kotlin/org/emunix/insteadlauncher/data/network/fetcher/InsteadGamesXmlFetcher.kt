/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.network.fetcher

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class InsteadGamesXmlFetcher(private val client: OkHttpClient): GameListFetcher {

    @Throws(IOException::class)
    override fun fetch(url: String): String {
        val request = Request.Builder()
            .url(url).build()
        val modRequest = Request.Builder()
            .url(url.replace("https://" , "http://" , true)).build()

        val response: Response = try {
            client.newCall(request).execute()
        } catch (ex: IOException) {
            client.newCall(modRequest).execute()
        }

        if (!response.isSuccessful) throw IOException("${response.code}")
        return response.body.string()
    }
}