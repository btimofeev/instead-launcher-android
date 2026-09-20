/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation

import android.os.Build
import javax.inject.Inject

interface PlatformInfo {

    val isDynamicColorsAvailable: Boolean
}

class AndroidPlatformInfo @Inject constructor() : PlatformInfo {

    override val isDynamicColorsAvailable: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // Android 12+
}