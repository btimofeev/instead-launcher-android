/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.model

sealed interface InstallGameResult {

    object Success : InstallGameResult

    data class Error(
        val type: Type,
        val throwable: Throwable? = null
    ) : InstallGameResult {

        enum class Type {

            DOWNLOAD_ERROR,

            UNPACKING_ERROR,

            GAME_NOT_FOUND_IN_DATABASE,
        }
    }
}