/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import java.io.IOException

/**
 * The object directly copies INSTEAD resources to the file system. Works in an IO stream.
 */
interface ResourceUpdater {

    @Throws(IOException::class)
    suspend fun update()
}