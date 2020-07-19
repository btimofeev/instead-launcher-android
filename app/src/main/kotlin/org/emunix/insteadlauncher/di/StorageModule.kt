/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.emunix.insteadlauncher.storage.Storage
import org.emunix.insteadlauncher.storage.StorageImpl
import javax.inject.Singleton

@Module
class StorageModule {

    @Provides
    @Singleton
    fun provideStorage(context: Context): Storage = StorageImpl(context)
}