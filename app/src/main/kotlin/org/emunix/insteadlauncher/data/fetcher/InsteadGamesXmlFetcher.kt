/*
 * Copyright (c) 2018, 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.fetcher

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class InsteadGamesXmlFetcher(private val client: OkHttpClient): GameListFetcher {

    @Throws(IOException::class)
    override fun fetch(url: String): String {
        val request = Request.Builder()
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("${response.code}")
        return response.body!!.string()
    }
}