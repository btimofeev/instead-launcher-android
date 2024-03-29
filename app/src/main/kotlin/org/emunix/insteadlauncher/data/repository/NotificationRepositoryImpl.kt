/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(): NotificationRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _downloadGame = MutableSharedFlow<DownloadGameStatus>(
        replay = 0,
        onBufferOverflow = DROP_OLDEST,
        extraBufferCapacity = 1
    )

    override val downloadGame: Flow<DownloadGameStatus> = _downloadGame.asSharedFlow()

    override fun publishDownloadGameStatus(status: DownloadGameStatus) {
        coroutineScope.launch {
            _downloadGame.emit(status)
        }
    }
}