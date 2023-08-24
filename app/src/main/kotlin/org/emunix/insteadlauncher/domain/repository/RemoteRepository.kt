/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import org.emunix.insteadlauncher.domain.model.GameModel
import java.io.InputStream

interface RemoteRepository {

    suspend fun download(url: String, gameName: String): InputStream

    suspend fun getGameList(): List<GameModel>
}