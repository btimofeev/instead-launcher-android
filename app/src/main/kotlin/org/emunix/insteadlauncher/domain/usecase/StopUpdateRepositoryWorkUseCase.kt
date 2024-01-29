/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.work.UpdateRepositoryWork
import javax.inject.Inject

/**
 * Stop the worker periodically updating the repository
 */
interface StopUpdateRepositoryWorkUseCase {

    operator fun invoke()
}

class StopUpdateRepositoryWorkUseCaseImpl @Inject constructor(
    private val updateRepositoryWork: UpdateRepositoryWork
) : StopUpdateRepositoryWorkUseCase {

    override fun invoke() {
        updateRepositoryWork.stop()
    }
}