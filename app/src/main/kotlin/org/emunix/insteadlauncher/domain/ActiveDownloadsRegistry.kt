/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveDownloadsRegistry @Inject constructor() {

    private val gameNames = mutableSetOf<String>()

    @Synchronized
    fun add(gameName: String) {
        gameNames.add(gameName)
    }

    @Synchronized
    fun remove(gameName: String) {
        gameNames.remove(gameName)
    }

    @Synchronized
    fun activeGameNames(): Set<String> = gameNames.toSet()
}
