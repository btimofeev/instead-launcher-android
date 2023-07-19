/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.data.repository.AppVersionRepositoryImpl
import org.emunix.insteadlauncher.data.repository.FileSystemRepositoryImpl
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.worker.UpdateRepositoryWorker
import org.emunix.insteadlauncher.services.UpdateRepositoryWorkManager

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindAppVersionRepository(impl: AppVersionRepositoryImpl): AppVersionRepository

    @Binds
    fun bindFileSystemRepository(impl: FileSystemRepositoryImpl): FileSystemRepository

    @Binds
    fun bindUpdateRepositoryWorker(impl: UpdateRepositoryWorkManager): UpdateRepositoryWorker
}