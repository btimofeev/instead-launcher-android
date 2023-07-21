/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import org.emunix.insteadlauncher.domain.usecase.CreateDirectoriesUseCase
import org.emunix.insteadlauncher.domain.usecase.CreateDirectoriesUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCase
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCaseImpl

@InstallIn(ViewModelComponent::class)
@Module
interface UseCaseModule {

    @Binds
    fun bindUpdateResourceUseCase(impl: UpdateResourceUseCaseImpl): UpdateResourceUseCase

    @Binds
    fun bindCreateDirectoriesUseCase(impl: CreateDirectoriesUseCaseImpl): CreateDirectoriesUseCase

    @Binds
    fun bindStartUpdateRepositoryWorkUseCase(impl: StartUpdateRepositoryWorkUseCaseImpl): StartUpdateRepositoryWorkUseCase

    @Binds
    fun bindStopUpdateRepositoryWorkUseCase(impl: StopUpdateRepositoryWorkUseCaseImpl): StopUpdateRepositoryWorkUseCase
}

@InstallIn(FragmentComponent::class)
@Module
interface UseCaseFragmentModule {

    @Binds
    fun bindStartUpdateRepositoryWorkUseCase(impl: StartUpdateRepositoryWorkUseCaseImpl): StartUpdateRepositoryWorkUseCase

    @Binds
    fun bindStopUpdateRepositoryWorkUseCase(impl: StopUpdateRepositoryWorkUseCaseImpl): StopUpdateRepositoryWorkUseCase
}

@InstallIn(ServiceComponent::class)
@Module
interface UseCaseServiceModule {

    @Binds
    fun bindScanAndUpdateLocalGamesUseCase(impl: ScanAndUpdateLocalGamesUseCaseImpl): ScanAndUpdateLocalGamesUseCase
}