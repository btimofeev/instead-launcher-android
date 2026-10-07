/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProviderImpl
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreferencesProviderImplTest {

    private val preferences: SharedPreferences = mockPreferences()
    private val preferencesProvider = PreferencesProviderImpl(preferences)

    @Test
    fun `controls hint is enabled by default`() {
        assertTrue(preferencesProvider.isGameControlsHintEnabled)
    }

    @Test
    fun `controls hint setting is persisted`() {
        preferencesProvider.isGameControlsHintEnabled = false
        assertFalse(preferencesProvider.isGameControlsHintEnabled)

        preferencesProvider.isGameControlsHintEnabled = true
        assertTrue(preferencesProvider.isGameControlsHintEnabled)
    }

    @Test
    fun `controls hint uses its own key`() {
        preferencesProvider.isMusicEnabled = false

        assertFalse(preferencesProvider.isMusicEnabled)
        assertTrue(preferencesProvider.isGameControlsHintEnabled)
    }

    private fun mockPreferences(): SharedPreferences {
        val values = mutableMapOf<String, Any?>()
        return mockk<SharedPreferences>().also { preferences ->
            every { preferences.getString(any(), any()) } answers { values[firstArg()] as? String ?: secondArg() }
            every { preferences.getBoolean(any(), any()) } answers { values[firstArg()] as? Boolean ?: secondArg() }
            every { preferences.contains(any()) } answers { values.containsKey(firstArg()) }
            every { preferences.edit() } answers {
                mockk<SharedPreferences.Editor>().also { editor ->
                    every { editor.putString(any(), any()) } answers {
                        values[firstArg()] = secondArg()
                        editor
                    }
                    every { editor.putBoolean(any(), any()) } answers {
                        values[firstArg()] = secondArg()
                        editor
                    }
                    every { editor.apply() } returns Unit
                }
            }
        }
    }
}
