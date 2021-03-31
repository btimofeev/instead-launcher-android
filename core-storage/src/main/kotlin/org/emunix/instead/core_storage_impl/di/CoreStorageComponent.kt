/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_storage_impl.di

import android.content.Context
import dagger.Component
import org.emunix.instead.core_storage_api.di.CoreStorageApi
import javax.inject.Singleton


@Component(modules = [StorageModule::class])
@Singleton
abstract class CoreStorageComponent : CoreStorageApi {
    companion object {
        @Volatile
        private var coreStorageComponent: CoreStorageComponent? = null

        fun get(context: Context): CoreStorageComponent {
            if (coreStorageComponent == null) {
                synchronized(CoreStorageComponent::class.java) {
                    if (coreStorageComponent == null) {
                        coreStorageComponent = DaggerCoreStorageComponent.builder()
                                .storageModule(StorageModule(context))
                                .build()
                    }
                }
            }
            return coreStorageComponent!!
        }
    }
}