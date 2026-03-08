/*
 * Copyright (c) 2018-2021, 2023, 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.launcher

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.domain.work.ScanGamesWork
import org.emunix.insteadlauncher.presentation.navigation.AppNavGraph
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private val appArgumentViewModel: AppArgumentViewModel by viewModels()

    @Inject
    lateinit var scanGamesWork: ScanGamesWork

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scanGamesWork.scan()

        val intent = intent
        if (intent.type == "application/zip") {
            intent.data?.let { uri ->
                appArgumentViewModel.zipUri = uri
            }
        }

        setContent {
            InsteadLauncherTheme {
                AppNavGraph(appArgumentViewModel)
            }
        }
    }
}
