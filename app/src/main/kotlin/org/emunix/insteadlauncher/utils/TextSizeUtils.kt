/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_TEXT_SIZE_STEP
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.MAX_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.MIN_INSTEAD_TEXT_SIZE
import kotlin.math.roundToInt

private const val ENGINE_BASE_TEXT_SIZE = 100

// Must mirror main.c: the engine honours only a positive value, truncates the division and clamps it.
internal fun snapTextSize(textSize: String): Int {
    val size = textSize.toIntOrNull()?.takeIf { it > 0 } ?: return ENGINE_BASE_TEXT_SIZE
    val scale = ((size - ENGINE_BASE_TEXT_SIZE) / INSTEAD_TEXT_SIZE_STEP)
        .coerceIn(
            (MIN_INSTEAD_TEXT_SIZE - ENGINE_BASE_TEXT_SIZE) / INSTEAD_TEXT_SIZE_STEP,
            (MAX_INSTEAD_TEXT_SIZE - ENGINE_BASE_TEXT_SIZE) / INSTEAD_TEXT_SIZE_STEP,
        )
    return ENGINE_BASE_TEXT_SIZE + scale * INSTEAD_TEXT_SIZE_STEP
}

// A slider reports a position just below the value it was aimed at, so it has to be rounded.
internal fun snapToSliderStep(value: Float, valueRange: IntRange, steps: Int): Int {
    val spacing = (valueRange.last - valueRange.first) / (steps + 1)
    val offset = ((value - valueRange.first) / spacing).roundToInt()
    return valueRange.first + offset * spacing
}
