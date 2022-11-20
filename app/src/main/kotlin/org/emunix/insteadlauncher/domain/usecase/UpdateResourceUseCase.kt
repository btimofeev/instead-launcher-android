/*
 * Copyright (c) 2021-2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

/**
 * Unpack application resources (INSTEAD directories) to the file system.
 */
interface UpdateResourceUseCase {

    /**
     * @param forceUpdate unpacking occurs every time if the flag is true
     * @return [UpdateResult]
     */
    suspend operator fun invoke(forceUpdate: Boolean = false): UpdateResult

    enum class UpdateResult {

        SUCCESS,

        NO_UPDATE_REQUIRED,

        ERROR,
    }
}