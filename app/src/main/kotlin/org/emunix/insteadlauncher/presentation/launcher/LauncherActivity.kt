/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.launcher

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.insteadlauncher.R
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

        setUpEdgeToEdge(binding)

        scanGamesWork.scan()

        val intent = intent
        if (intent.type == "application/zip") {
            intent.data?.let { uri ->
                appArgumentViewModel.zipUri = uri
            }
        }
    }

    private fun setUpEdgeToEdge(binding: ActivityLauncherBinding) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val barInsets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(barInsets.left, barInsets.top, barInsets.right, barInsets.bottom)
            insets
        }
    }
}
