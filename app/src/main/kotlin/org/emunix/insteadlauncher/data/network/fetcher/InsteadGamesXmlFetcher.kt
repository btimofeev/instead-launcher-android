/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.network.fetcher

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

class InsteadGamesXmlFetcher(private val client: OkHttpClient): GameListFetcher {

    @Throws(IOException::class)
    override fun fetch(url: String, isRedirectToHttp: Boolean): String {
        val request = Request.Builder()
            .url(url).build()
        val modRequest = Request.Builder()
            .url(url.replace("https://" , "http://" , true)).build()

        val response: Response = if (isRedirectToHttp)
            try {
                client.newCall(request).execute()
            } catch (ex: SocketTimeoutException) {
                client.newCall(modRequest).execute()
            }
        else
            client.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("${response.code}")
        return response.body.string()
    }
}