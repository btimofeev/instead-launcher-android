/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_preferences.utils

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun SharedPreferences.asFlow(
    key: String,
    defaultValue: Boolean = false
): Flow<Boolean> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            trySend(getBoolean(key, defaultValue))
        }
    }

    trySend(getBoolean(key, defaultValue))
    registerOnSharedPreferenceChangeListener(listener)

    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}