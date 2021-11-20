/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.manager.repository

import android.content.Context
import org.emunix.insteadlauncher.helpers.isServiceRunning
import org.emunix.insteadlauncher.services.UpdateRepository
import javax.inject.Inject

class RepositoryManagerImpl @Inject constructor(
    private val context: Context,
) : RepositoryManager {

    override fun updateRepository() {
        UpdateRepository.start(context)
    }

    override fun isRepositoryUpdating(): Boolean =
        context.isServiceRunning(UpdateRepository::class.java)
}