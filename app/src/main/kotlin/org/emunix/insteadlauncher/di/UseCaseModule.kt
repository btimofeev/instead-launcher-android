/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCaseImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface UseCaseModule {

    @Singleton
    @Binds
    fun bindUpdateResourceUseCase(impl: UpdateResourceUseCaseImpl): UpdateResourceUseCase
}