/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.services.DeleteGame

class DeleteGameDialog : DialogFragment() {

    companion object {
        fun newInstance(gameName: String): DeleteGameDialog {
            val fragment = DeleteGameDialog()
            val args = Bundle()
            args.putString("game", gameName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val game = arguments!!.getString("game")
        return AlertDialog.Builder(activity!!, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setTitle(R.string.dialog_delete_game_title)
                .setMessage(R.string.dialog_delete_game_text)
                .setPositiveButton(R.string.dialog_delete_game_positive_button) { _, _ ->
                    DeleteGame.start(activity!!.applicationContext, game)
                }
                .setNegativeButton(R.string.dialog_delete_game_negative_button) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
    }
}