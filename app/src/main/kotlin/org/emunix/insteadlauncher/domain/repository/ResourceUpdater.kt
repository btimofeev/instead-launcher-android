/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import java.io.IOException

/**
 * Updates INSTEAD resources in the file system (by copying from APK). Works in an IO thread.
 */
interface ResourceUpdater {

    @Throws(IOException::class)
    suspend fun update()
}