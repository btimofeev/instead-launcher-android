/*
 * Copyright (c) 2021-2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import org.emunix.insteadlauncher.domain.repository.ResourceUpdater
import timber.log.Timber
import javax.inject.Inject

class UpdateResourceUseCaseImpl @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val resourceUpdater: ResourceUpdater,
) : UpdateResourceUseCase {

    override suspend fun execute(isDebugBuild: Boolean): Boolean {
        if (appVersionRepository.isNewVersion() || isDebugBuild) {
            runCatching {
                resourceUpdater.update()
                appVersionRepository.saveCurrentVersion()
            }.onFailure {
                Timber.e(it)
                return false
            }
        }
        return true
    }
}