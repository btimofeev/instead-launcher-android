/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

/**
 * Unpack application resources (INSTEAD directories) to the file system.
 * Resources are unpacked only if the version of the application has been updated or if it is a Debug build
 */
interface UpdateResourceUseCase {

    /**
     * @param isDebugBuild unpacking occurs every time if the flag is true
     * @return true if resources have been updated or updates are not required. False on copy error
     */
    suspend fun execute(isDebugBuild: Boolean): Boolean
}