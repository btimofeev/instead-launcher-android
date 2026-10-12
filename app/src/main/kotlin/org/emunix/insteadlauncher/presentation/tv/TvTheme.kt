/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme as AppMaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.darkColorScheme

@Composable
fun TvTheme(content: @Composable () -> Unit) {
    val appColors = AppMaterialTheme.colorScheme
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.onPrimary,
            background = appColors.background,
            onBackground = appColors.onBackground,
            surface = appColors.surface,
            onSurface = appColors.onSurface,
            surfaceVariant = appColors.surfaceVariant,
            onSurfaceVariant = appColors.onSurfaceVariant,
            secondary = appColors.secondary,
            onSecondary = appColors.onSecondary,
            tertiary = appColors.tertiary,
            onTertiary = appColors.onTertiary,
            error = appColors.error,
            onError = appColors.onError,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ),
        ) {
            content()
        }
    }
}
