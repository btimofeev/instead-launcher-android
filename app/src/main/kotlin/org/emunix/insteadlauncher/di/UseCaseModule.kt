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
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.domain.usecase.CreateDirectoriesUseCase
import org.emunix.insteadlauncher.domain.usecase.CreateDirectoriesUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.DeleteGameUseCase
import org.emunix.insteadlauncher.domain.usecase.DeleteGameUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.GetGameInfoFlowUseCase
import org.emunix.insteadlauncher.domain.usecase.GetGameInfoFlowUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.GetGamesFlowUseCase
import org.emunix.insteadlauncher.domain.usecase.GetGamesFlowUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.InstallGameUseCase
import org.emunix.insteadlauncher.domain.usecase.InstallGameUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCase
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.SearchGamesUseCase
import org.emunix.insteadlauncher.domain.usecase.SearchGamesUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCaseImpl
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCaseImpl
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

    @Binds
    fun bindGetDownloadGamesStatusUseCase(impl: GetDownloadGamesStatusUseCaseImpl): GetDownloadGamesStatusUseCase

    @Binds
    fun bindGetGamesFlowUseCase(impl: GetGamesFlowUseCaseImpl): GetGamesFlowUseCase

    @Binds
    fun bindSearchGamesUseCase(impl: SearchGamesUseCaseImpl): SearchGamesUseCase

    @Binds
    fun bindGetGameInfoFlowUseCase(impl: GetGameInfoFlowUseCaseImpl): GetGameInfoFlowUseCase
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
    fun bindDeleteGameUseCase(impl: DeleteGameUseCaseImpl): DeleteGameUseCase

    @Binds
    fun bindGetDownloadGamesStatusUseCase(impl: GetDownloadGamesStatusUseCaseImpl): GetDownloadGamesStatusUseCase

    @Binds
    fun bindInstallGameUseCase(impl: InstallGameUseCaseImpl): InstallGameUseCase
}

@InstallIn(SingletonComponent::class)
@Module
interface UseCaseSingletonModule {

    @Binds
    fun bindScanAndUpdateLocalGamesUseCase(impl: ScanAndUpdateLocalGamesUseCaseImpl): ScanAndUpdateLocalGamesUseCase

    @Binds
    fun bindUpdateGameListUseCase(impl: UpdateGameListUseCaseImpl): UpdateGameListUseCase
}