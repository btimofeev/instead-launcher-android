/*
 * Copyright (c) 2019-2021, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.dialogs

import android.app.Dialog
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

class DeleteGameDialog(
    private val gameManager: GameManager
) : DialogFragment() {

    companion object {
        fun newInstance(gameName: String, gameManager: GameManager): DeleteGameDialog {
            val fragment = DeleteGameDialog(gameManager)
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
                    gameManager.deleteGame(game)
                }
                .setNegativeButton(R.string.dialog_delete_game_negative_button) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
    }
}

@Composable
fun DeleteGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
        title = { Text(text = stringResource(R.string.dialog_delete_game_title)) },
        text = { Text(text = stringResource(R.string.dialog_delete_game_text)) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(stringResource(R.string.dialog_delete_game_positive_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(R.string.dialog_delete_game_negative_button))
            }
        }
    )
}

@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Preview(showBackground = true, widthDp = 400)
@Composable
private fun DeleteGameDialogPreview() {
    InsteadLauncherTheme {
        DeleteGameDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}