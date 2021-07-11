/*
 * Copyright (c) 2019-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.interactor.GamesInteractor

class DeleteGameDialog(
    private val gamesInteractor: GamesInteractor
) : DialogFragment() {

    companion object {
        fun newInstance(gameName: String, gamesInteractor: GamesInteractor): DeleteGameDialog {
            val fragment = DeleteGameDialog(gamesInteractor)
            val args = Bundle()
            args.putString("game", gameName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val game = requireArguments().getString("game")
        require(game != null)
        return MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_AlertDialogOverlay)
                .setTitle(R.string.dialog_delete_game_title)
                .setMessage(R.string.dialog_delete_game_text)
                .setPositiveButton(R.string.dialog_delete_game_positive_button) { _, _ ->
                    gamesInteractor.deleteGame(game)
                }
                .setNegativeButton(R.string.dialog_delete_game_negative_button) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
    }
}