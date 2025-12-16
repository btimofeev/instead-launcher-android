/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

import androidx.annotation.DrawableRes

sealed interface SettingsItem {

    data class Category(val title: String): SettingsItem

    data class Element(
        val id: String,
        @DrawableRes
        val icon: Int? = null,
        val title: String,
        val description: String? = null,
        val switchState: Boolean? = null,
        val isEnabled: Boolean = true,
        val onClick: () -> Unit,
    ): SettingsItem

    data object Divider: SettingsItem
}