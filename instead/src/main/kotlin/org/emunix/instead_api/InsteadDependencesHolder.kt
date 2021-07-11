/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead_api

import android.content.SharedPreferences
import org.emunix.instead.core_storage_api.data.Storage

interface InsteadDependenciesHolder {

    val storage: Storage
    val preferences: SharedPreferences
}