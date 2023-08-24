/*
 * Copyright (c) 2020-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.emunix.instead.InsteadApiImpl
import org.emunix.instead_api.InsteadApi
import org.emunix.insteadlauncher.helpers.resourceprovider.ResourceProvider
import org.emunix.insteadlauncher.helpers.resourceprovider.ResourceProviderImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    fun provideFeatureInstead(context: Context): InsteadApi = InsteadApiImpl(context)

    @Provides
    fun provideResourceProvider(context: Context): ResourceProvider = ResourceProviderImpl(context)
}