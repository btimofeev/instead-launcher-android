/*
 * Copyright (c) 2020-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.emunix.instead.InsteadApiImpl
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead.core_storage_impl.di.CoreStorageComponent
import org.emunix.instead_api.InsteadApi
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.helpers.eventbus.RxBus
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideEventBus(): EventBus = RxBus

    @Provides
    @Singleton
    fun provideStorage(): Storage = CoreStorageComponent.get(context).storage()

    @Provides
    fun provideFeatureInstead(): InsteadApi = InsteadApiImpl()
}