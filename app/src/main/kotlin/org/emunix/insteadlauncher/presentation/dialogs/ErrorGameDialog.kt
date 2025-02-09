/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(Icons.Rounded.ErrorOutline, contentDescription = null) },
        title = { Text(text = stringResource(R.string.error)) },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = message,
                textAlign = TextAlign.Center,
            )
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.dialog_error_close_button))
            }
        },
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
private fun ErrorDialogPreview() {
    InsteadLauncherTheme {
        ErrorDialog(
            message = "Произошла ошибка",
            onDismiss = {},
        )
    }
}