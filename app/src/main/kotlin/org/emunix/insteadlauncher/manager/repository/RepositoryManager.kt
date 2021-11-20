/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.manager.repository

interface RepositoryManager {

    /**
     * Fetch game list from network repository
     */
    fun updateRepository()

    /**
     * Find out if the process of updating the repository is currently running
     */
    fun isRepositoryUpdating(): Boolean
}