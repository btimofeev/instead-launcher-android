/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.compose

import android.content.res.Configuration
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TvModeTest {

    @Test
    fun `detects the television ui mode`() {
        assertTrue(isTelevisionUiMode(Configuration.UI_MODE_TYPE_TELEVISION))
    }

    @Test
    fun `detects the television ui mode together with the night bit`() {
        assertTrue(
            isTelevisionUiMode(
                Configuration.UI_MODE_TYPE_TELEVISION or Configuration.UI_MODE_NIGHT_YES
            )
        )
    }

    @Test
    fun `ignores other ui modes`() {
        listOf(
            Configuration.UI_MODE_TYPE_NORMAL,
            Configuration.UI_MODE_TYPE_DESK,
            Configuration.UI_MODE_TYPE_CAR,
            Configuration.UI_MODE_TYPE_WATCH,
            Configuration.UI_MODE_TYPE_APPLIANCE,
        ).forEach { type ->
            assertFalse(isTelevisionUiMode(type), "uiModeType $type")
            assertFalse(
                isTelevisionUiMode(type or Configuration.UI_MODE_NIGHT_YES),
                "uiModeType $type with night",
            )
        }
    }
}
