/*
 * Copyright (c) 2018-2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.databinding.FragmentGameBinding
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.model.GameState.IN_QUEUE_TO_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.IS_DELETE
import org.emunix.insteadlauncher.domain.model.GameState.IS_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.NO_INSTALLED
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.presentation.models.DownloadState
import org.emunix.insteadlauncher.presentation.models.GameInfo
import org.emunix.insteadlauncher.utils.launchBrowser
import org.emunix.insteadlauncher.utils.loadUrl
import javax.inject.Inject

@AndroidEntryPoint
class GameFragment : Fragment(R.layout.fragment_game) {

    @Inject
    lateinit var gameManager: GameManager

    private val viewModel: GameViewModel by viewModels()

    private val binding by viewBinding(FragmentGameBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_24dp)
        binding.toolbar.setNavigationOnClickListener { closeScreen() }

        val gameName = arguments?.getString("game_name")
            ?: throw IllegalArgumentException("GameFragment require game_name passed as argument")
        viewModel.init(gameName)

        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.game.collect { game ->
                        if (game != null) {
                            binding.setViews(game)
                        }
                    }
                }

                launch {
                    viewModel.closeScreenCommand.collect {
                        closeScreen()
                    }
                }

                launch {
                    viewModel.downloadState.collect { state ->
                        setDownloadState(state)
                    }
                }

                launch {
                    viewModel.downloadErrorCommand.collect { command ->
                        context?.showErrorDialog(command.message)
                    }
                }
            }
        }
    }

    private fun FragmentGameBinding.setViews(game: GameInfo) {
        val activity = activity as? AppCompatActivity ?: return
        activity.supportActionBar?.title = ""
        collapsingToolbar?.isTitleEnabled = false

        name.text = game.title
        author.text = game.author
        version.text = game.version
        size.text = game.size
        gameImage.loadUrl(url = game.imageUrl, highQuality = true)
        description.text = game.description

        if (game.siteUrl.isNotBlank()) {
            feedbackButton.isVisible = true
            feedbackButton.setOnClickListener { activity.launchBrowser(game.siteUrl) }
        } else {
            feedbackButton.isVisible = false
        }

        if (game.state == INSTALLED) {
            installMessage.isVisible = false
            progressBar.isVisible = false
            installButton.isVisible = false
            deleteButton.isVisible = true
            runButton.isVisible = true

            if (game.isUpdateButtonShow) {
                installButton.text = getText(R.string.game_activity_button_update)
                installButton.backgroundTintList =
                    ContextCompat.getColorStateList(activity, R.color.colorUpdateButton)
                installButton.isVisible = true
            }
        }

        if (game.state == NO_INSTALLED) {
            installButton.text = getText(R.string.game_activity_button_install)
            installButton.backgroundTintList =
                ContextCompat.getColorStateList(activity, R.color.colorInstallButton)
            installButton.isVisible = true
            deleteButton.isVisible = false
            runButton.isVisible = false
            progressBar.isVisible = false
            installMessage.isVisible = false
        }

        if (game.state == IS_INSTALL) {
            installMessage.text = getString(R.string.game_activity_message_installing)
            showProgress(true)
        }

        if (game.state == IS_DELETE) {
            installMessage.text = getString(R.string.notification_delete_game)
            showProgress(true)
        }

        if (game.state == IN_QUEUE_TO_INSTALL) {
            installMessage.text = getString(R.string.game_activity_message_download_pending)
            showProgress(true)
        }

        installButton.setOnClickListener {
            viewModel.installGame()
        }

        deleteButton.setOnClickListener {
            if (game.state == INSTALLED) {
                if (isAdded) {
                    val deleteDialog = DeleteGameDialog.newInstance(game.name, gameManager)
                    parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
                }
            }
        }

        runButton.setOnClickListener {
            viewModel.runGame()
        }
    }

    private fun showProgress(flag: Boolean) {
        with(binding) {
            installMessage.isVisible = flag
            progressBar.isVisible = flag
            installButton.isVisible = !flag
            deleteButton.isVisible = !flag
            runButton.isVisible = !flag
        }
    }

    private fun setIndeterminateProgress(indeterminate: Boolean, value: Int = 0) {
        binding.progressBar.isIndeterminate = indeterminate
        binding.progressBar.progress = value
    }

    private fun setInstallMessage(msg: String) {
        binding.installMessage.text = msg
    }

    private fun closeScreen() {
        findNavController().popBackStack()
    }

    private fun setDownloadState(state: DownloadState?) {
        if (state != null) {
            setInstallMessage(state.message)
            if (state.progress == -1) {
                setIndeterminateProgress(true)
            } else {
                setIndeterminateProgress(false, state.progress)
            }
        }
    }

    private fun Context.showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(string.error)
            .setMessage(message)
            .setPositiveButton(string.dialog_error_close_button) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
