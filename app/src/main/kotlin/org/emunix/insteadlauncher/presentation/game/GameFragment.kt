/*
 * Copyright (c) 2018-2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import android.content.Intent
import android.net.Uri
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

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
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
                            setViews(game)
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
                        showErrorDialog(command.message)
                    }
                }
            }
        }
    }

    private fun setViews(game: GameInfo) {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.title = ""
        binding.collapsingToolbar?.isTitleEnabled = false

        binding.name.text = game.title
        binding.author.text = game.author
        binding.version.text = game.version
        binding.size.text = game.size
        binding.gameImage.loadUrl(url = game.imageUrl, highQuality = true)
        binding.description.text = game.description

        if (game.siteUrl.isNotBlank()) {
            binding.feedbackButton.isVisible = true
            binding.feedbackButton.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(game.siteUrl))
                browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                requireActivity().startActivity(browserIntent)
            }
        } else {
            binding.feedbackButton.isVisible = false
        }

        if (game.state == INSTALLED) {
            binding.installMessage.isVisible = false
            binding.progressBar.isVisible = false
            binding.installButton.isVisible = false
            binding.deleteButton.isVisible = true
            binding.runButton.isVisible = true

            if (game.isUpdateButtonShow) {
                binding.installButton.text = getText(R.string.game_activity_button_update)
                binding.installButton.backgroundTintList =
                    ContextCompat.getColorStateList(activity, R.color.colorUpdateButton)
                binding.installButton.isVisible = true
            }
        }

        if (game.state == NO_INSTALLED) {
            binding.installButton.text = getText(R.string.game_activity_button_install)
            binding.installButton.backgroundTintList =
                ContextCompat.getColorStateList(activity, R.color.colorInstallButton)
            binding.installButton.isVisible = true
            binding.deleteButton.isVisible = false
            binding.runButton.isVisible = false
            binding.progressBar.isVisible = false
            binding.installMessage.isVisible = false
        }

        if (game.state == IS_INSTALL) {
            binding.installMessage.text = getString(R.string.game_activity_message_installing)
            showProgress(true)
        }

        if (game.state == IS_DELETE) {
            binding.installMessage.text = getString(R.string.notification_delete_game)
            showProgress(true)
        }

        if (game.state == IN_QUEUE_TO_INSTALL) {
            binding.installMessage.text = getString(R.string.game_activity_message_download_pending)
            showProgress(true)
        }

        binding.installButton.setOnClickListener {
            viewModel.installGame()
        }

        binding.deleteButton.setOnClickListener {
            if (game.state == INSTALLED) {
                if (isAdded) {
                    val deleteDialog = DeleteGameDialog.newInstance(game.name, gameManager)
                    parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
                }
            }
        }

        binding.runButton.setOnClickListener {
            viewModel.runGame()
        }
    }

    private fun showProgress(flag: Boolean) {
        binding.installMessage.isVisible = flag
        binding.progressBar.isVisible = flag
        binding.installButton.isVisible = !flag
        binding.deleteButton.isVisible = !flag
        binding.runButton.isVisible = !flag
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

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(string.error)
            .setMessage(message)
            .setPositiveButton(string.dialog_error_close_button) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
