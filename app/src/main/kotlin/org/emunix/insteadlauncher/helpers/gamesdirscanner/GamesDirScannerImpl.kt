/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.gamesdirscanner

import android.content.Context
import org.emunix.insteadlauncher.services.ScanGames
import javax.inject.Inject

class GamesDirScannerImpl @Inject constructor(private val context: Context) : GamesDirScanner {

    override fun scan() {
        ScanGames.start(context)
    }
}