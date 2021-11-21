/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

/**
 * Repository providing information about the version of the application
 */
interface AppVersionRepository {

    /**
     * Get internal application version code
     */
    val versionCode: Long

    /**
     * Get application version visible to the user
     */
    val versionName: String

    /**
     * Has the application version been updated since the last time you saved the version in the settings?
     *
     * @return  true if the application was updated since the last version was saved in the settings
     */
    fun isNewVersion(): Boolean

    /**
     * Save the current version of the application in the settings
     */
    fun saveCurrentVersion()
}