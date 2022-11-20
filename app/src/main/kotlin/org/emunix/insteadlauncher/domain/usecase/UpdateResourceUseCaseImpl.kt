/*
 * Copyright (c) 2021-2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import org.emunix.insteadlauncher.domain.repository.ResourceUpdater
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.ERROR
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.NO_UPDATE_REQUIRED
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.SUCCESS
import timber.log.Timber
import javax.inject.Inject

class UpdateResourceUseCaseImpl @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val resourceUpdater: ResourceUpdater,
) : UpdateResourceUseCase {

    override suspend fun invoke(forceUpdate: Boolean): UpdateResult {
        return if (appVersionRepository.isNewVersion() || forceUpdate) {
            updateResourcesAndGetResult()
        } else {
            NO_UPDATE_REQUIRED
        }
    }

    private suspend fun updateResourcesAndGetResult(): UpdateResult {
        var result = ERROR

        runCatching {
            resourceUpdater.update()
            appVersionRepository.saveCurrentVersion()
        }
            .onSuccess { result = SUCCESS }
            .onFailure { Timber.e(it) }

        return result
    }
}