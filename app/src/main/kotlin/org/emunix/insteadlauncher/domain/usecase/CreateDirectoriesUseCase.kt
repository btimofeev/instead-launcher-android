/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import java.io.IOException
import javax.inject.Inject

interface CreateDirectoriesUseCase {

    @Throws(IOException::class)
    suspend operator fun invoke()
}

class CreateDirectoriesUseCaseImpl @Inject constructor(
    private val fileSystemRepository: FileSystemRepository,
) : CreateDirectoriesUseCase {

    override suspend fun invoke() {
        fileSystemRepository.createStorageDirectories()
    }
}