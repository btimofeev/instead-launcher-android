/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.model

sealed interface UpdateGameListResult {

    object Success : UpdateGameListResult

    data class Error(val e: Throwable) : UpdateGameListResult
}