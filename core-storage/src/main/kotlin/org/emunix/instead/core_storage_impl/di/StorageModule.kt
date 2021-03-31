/*
 * Copyright (c) 2020-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_storage_impl.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead.core_storage_impl.data.StorageImpl
import javax.inject.Singleton

@Module
internal class StorageModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideStorage(): Storage = StorageImpl(context)
}