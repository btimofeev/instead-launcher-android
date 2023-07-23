/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import kotlinx.coroutines.flow.Flow
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus

interface NotificationRepository {

    val downloadGame: Flow<DownloadGameStatus>

    suspend fun publishDownloadGameStatus(status: DownloadGameStatus)
}