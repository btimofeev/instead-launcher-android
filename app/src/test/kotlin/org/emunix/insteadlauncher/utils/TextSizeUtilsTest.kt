/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.MAX_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.MIN_INSTEAD_TEXT_SIZE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextSizeUtilsTest {

    @Test
    fun `keeps a value the slider can land on`() {
        assertEquals(130, snapTextSize("130"))
        assertEquals(100, snapTextSize("100"))
        assertEquals(50, snapTextSize("50"))
        assertEquals(400, snapTextSize("400"))
    }

    @Test
    fun `truncates the way the engine truncates, in both directions`() {
        assertEquals(130, snapTextSize("139"))
        assertEquals(130, snapTextSize("135"))
        assertEquals(80, snapTextSize("75"))
        assertEquals(100, snapTextSize("91"))
        assertEquals(60, snapTextSize("51"))
    }

    @Test
    fun `clamps to the bounds the engine honours`() {
        assertEquals(MAX_INSTEAD_TEXT_SIZE, snapTextSize("9999"))
        assertEquals(MIN_INSTEAD_TEXT_SIZE, snapTextSize("1"))
    }

    @Test
    fun `keeps the size the engine falls back to for a value it does not honour`() {
        assertEquals(100, snapTextSize(""))
        assertEquals(100, snapTextSize("abc"))
        assertEquals(100, snapTextSize("0"))
        assertEquals(100, snapTextSize("-40"))
    }

    @Test
    fun `rounds a slider position up to the nearest step instead of down`() {
        assertEquals(200, snapToSliderStep(199.99999f, 50..400, 34))
        assertEquals(400, snapToSliderStep(399.99997f, 50..400, 34))
        assertEquals(130, snapToSliderStep(129.99999f, 50..400, 34))
    }

    @Test
    fun `rounds a slider position down when it lands below the middle of a step`() {
        assertEquals(130, snapToSliderStep(134.9f, 50..400, 34))
        assertEquals(140, snapToSliderStep(135.1f, 50..400, 34))
    }
}
