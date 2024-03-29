/*
 * Copyright (c) 2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.data.repository.AppVersionRepositoryImpl
import org.emunix.insteadlauncher.data.repository.DataBaseRepositoryImpl
import org.emunix.insteadlauncher.data.repository.FileSystemRepositoryImpl
import org.emunix.insteadlauncher.data.repository.NotificationRepositoryImpl
import org.emunix.insteadlauncher.data.repository.RemoteRepositoryImpl
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import org.emunix.insteadlauncher.domain.work.DeleteGameWork
import org.emunix.insteadlauncher.domain.work.ScanGamesWork
import org.emunix.insteadlauncher.domain.work.UpdateRepositoryWork
import org.emunix.insteadlauncher.services.DeleteGameWorkImpl
import org.emunix.insteadlauncher.services.ScanGamesWorkImpl
import org.emunix.insteadlauncher.services.UpdateRepositoryWorkImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindAppVersionRepository(impl: AppVersionRepositoryImpl): AppVersionRepository

    @Binds
    @Singleton
    fun bindFileSystemRepository(impl: FileSystemRepositoryImpl): FileSystemRepository

    @Binds
    fun bindUpdateRepositoryWorker(impl: UpdateRepositoryWorkImpl): UpdateRepositoryWork

    @Binds
    fun bindDeleteGameWork(impl: DeleteGameWorkImpl): DeleteGameWork

    @Binds
    fun bindScanGamesWork(impl: ScanGamesWorkImpl): ScanGamesWork

    @Binds
    @Singleton
    fun bindDataBaseRepository(impl: DataBaseRepositoryImpl): DataBaseRepository

    @Binds
    @Singleton
    fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    fun bindRemoteRepository(impl: RemoteRepositoryImpl): RemoteRepository
}