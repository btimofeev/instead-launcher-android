/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCaseImpl

@InstallIn(SingletonComponent::class)
@Module
interface UseCaseModule {

    @Binds
    fun bindUpdateResourceUseCase(impl: UpdateResourceUseCaseImpl): UpdateResourceUseCase

    @Binds
    fun bindStartUpdateRepositoryWorkUseCase(impl: StartUpdateRepositoryWorkUseCaseImpl): StartUpdateRepositoryWorkUseCase

    @Binds
    fun bindStopUpdateRepositoryWorkUseCase(impl: StopUpdateRepositoryWorkUseCaseImpl): StopUpdateRepositoryWorkUseCase
}