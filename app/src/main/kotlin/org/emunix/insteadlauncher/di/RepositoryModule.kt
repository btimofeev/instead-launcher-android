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
import org.emunix.insteadlauncher.data.repository.ResourceUpdaterImpl
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import org.emunix.insteadlauncher.domain.repository.ResourceUpdater
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindAppVersionRepository(impl: AppVersionRepositoryImpl): AppVersionRepository

    @Singleton
    @Binds
    fun bindResourceUpdater(impl:  ResourceUpdaterImpl): ResourceUpdater
}