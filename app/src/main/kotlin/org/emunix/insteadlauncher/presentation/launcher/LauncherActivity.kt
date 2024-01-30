/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.launcher

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.databinding.ActivityLauncherBinding
import org.emunix.insteadlauncher.domain.work.ScanGamesWork
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private val appArgumentViewModel: AppArgumentViewModel by viewModels()

    @Inject
    lateinit var scanGamesWork: ScanGamesWork

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanGamesWork.scan()

        val intent = intent
        if (intent.type == "application/zip") {
            intent.data?.let { uri ->
                appArgumentViewModel.zipUri = uri
            }
        }
    }
}
