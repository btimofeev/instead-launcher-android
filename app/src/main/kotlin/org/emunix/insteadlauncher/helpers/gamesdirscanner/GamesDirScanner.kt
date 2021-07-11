/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.gamesdirscanner

interface GamesDirScanner {

    /**
     * Scan the games directory.
     * Searches for locally installed games and games that have been removed and updates the information in the database
     */
    fun scan()
}