/*
 * Copyright (c) 2020-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_storage_api.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead.core_storage_api.data.StorageImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class StorageModule() {

    @Provides
    @Singleton
    fun provideStorage(@ApplicationContext context: Context): Storage = StorageImpl(context)
}