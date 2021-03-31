/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_storage_api.di

import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead.module_injector.BaseAPI

interface CoreStorageApi: BaseAPI {
    fun storage(): Storage
}