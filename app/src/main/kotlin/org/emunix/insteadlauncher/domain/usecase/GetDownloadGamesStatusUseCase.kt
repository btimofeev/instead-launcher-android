/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import javax.inject.Inject

interface GetDownloadGamesStatusUseCase {

    operator fun invoke(): Flow<DownloadGameStatus>
}

class GetDownloadGamesStatusUseCaseImpl @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : GetDownloadGamesStatusUseCase {

    override fun invoke(): Flow<DownloadGameStatus> = notificationRepository.downloadGame
}