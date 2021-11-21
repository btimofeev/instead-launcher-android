/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.game

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.Game.State.INSTALLED
import org.emunix.insteadlauncher.data.db.Game.State.IN_QUEUE_TO_INSTALL
import org.emunix.insteadlauncher.data.db.Game.State.IS_DELETE
import org.emunix.insteadlauncher.data.db.Game.State.IS_INSTALL
import org.emunix.insteadlauncher.data.db.Game.State.NO_INSTALLED
import org.emunix.insteadlauncher.databinding.FragmentGameBinding
import org.emunix.insteadlauncher.helpers.loadUrl
import org.emunix.insteadlauncher.helpers.showToast
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.ui.dialogs.DeleteGameDialog
import javax.inject.Inject

@AndroidEntryPoint
class GameFragment : Fragment() {

    @Inject
    lateinit var gameManager: GameManager

    private val viewModel: GameViewModel by viewModels()

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_24dp)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val gameName = arguments?.getString("game_name")
            ?: throw IllegalArgumentException("GameFragment require game_name passed as argument")
        viewModel.init(gameName)

        viewModel.getGame().observe(viewLifecycleOwner) { game ->
            if (game != null) {
                setViews(game)
            } else {
                activity?.finish()
            }
        }
        viewModel.getProgress().observe(viewLifecycleOwner) { value ->
            if (value == -1) {
                setIndeterminateProgress(true)
            } else {
                setIndeterminateProgress(false, value)
            }
        }
        viewModel.getProgressMessage().observe(viewLifecycleOwner) { msg ->
            setInstallMessage(msg)
        }
        viewModel.getErrorMessage().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                requireContext().showToast(it)
            }
        }
    }

    private fun setViews(game: Game) {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.title = ""
        binding.collapsingToolbar?.isTitleEnabled = false

        binding.name.text = game.title
        binding.author.text = game.author
        if (game.installedVersion.isNotBlank() and (game.version != game.installedVersion)) {
            binding.version.text =
                getString(R.string.game_activity_label_version, game.installedVersion + " (\u2191${game.version})")
        } else {
            binding.version.text = getString(R.string.game_activity_label_version, game.version)
        }
        binding.size.text = getString(R.string.game_activity_label_size, FileUtils.byteCountToDisplaySize(game.size))
        binding.gameImage.loadUrl(game.image)
        binding.description.text = game.description

        if (game.descurl.isNotBlank()) {
            binding.feedbackButton.visible(true)
            binding.feedbackButton.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(game.descurl))
                browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                requireActivity().startActivity(browserIntent)
            }
        } else {
            binding.feedbackButton.visible(false)
        }

        if (game.state == INSTALLED) {
            binding.installMessage.visible(false)
            binding.progressBar.visible(false)
            binding.installButton.visible(false)
            binding.deleteButton.visible(true)
            binding.runButton.visible(true)

            if (game.version != game.installedVersion) {
                binding.installButton.text = getText(R.string.game_activity_button_update)
                binding.installButton.backgroundTintList =
                    ContextCompat.getColorStateList(activity, R.color.colorUpdateButton)
                binding.installButton.visible(true)
            }
        }

        if (game.state == NO_INSTALLED) {
            binding.installButton.text = getText(R.string.game_activity_button_install)
            binding.installButton.backgroundTintList =
                ContextCompat.getColorStateList(activity, R.color.colorInstallButton)
            binding.installButton.visible(true)
            binding.deleteButton.visible(false)
            binding.runButton.visible(false)
            binding.progressBar.visible(false)
            binding.installMessage.visible(false)
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
                val deleteDialog = DeleteGameDialog.newInstance(game.name, gameManager)
                if (isAdded)
                    parentFragmentManager.let { deleteDialog.show(it, "delete_dialog") }
            }
        }

        binding.runButton.setOnClickListener {
            viewModel.runGame()
        }
    }

    private fun showProgress(flag: Boolean) {
        binding.installMessage.visible(flag)
        binding.progressBar.visible(flag)
        binding.installButton.visible(!flag)
        binding.deleteButton.visible(!flag)
        binding.runButton.visible(!flag)
    }

    private fun setIndeterminateProgress(indeterminate: Boolean, value: Int = 0) {
        binding.progressBar.isIndeterminate = indeterminate
        binding.progressBar.progress = value
    }

    private fun setInstallMessage(msg: String) {
        binding.installMessage.text = msg
    }
}
