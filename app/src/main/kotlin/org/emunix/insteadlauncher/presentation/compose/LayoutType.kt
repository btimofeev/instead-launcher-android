/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

sealed class LayoutType {
    object PhonePortrait : LayoutType()
    object PhoneLandscape : LayoutType()
    object TabletPortrait : LayoutType()
    object TabletLandscape : LayoutType()
}

@Composable
fun rememberLayoutType(): LayoutType {
    val windowSize = LocalWindowInfo.current.containerDpSize
    val smallestWidth = minOf(windowSize.width, windowSize.height)
    val isTablet = smallestWidth > 600.dp
    val isLandscape = windowSize.width > windowSize.height

    return remember(windowSize) {
        when {
            isTablet && isLandscape -> LayoutType.TabletLandscape
            isTablet && !isLandscape -> LayoutType.TabletPortrait
            !isTablet && isLandscape -> LayoutType.PhoneLandscape
            else -> LayoutType.PhonePortrait
        }
    }
}