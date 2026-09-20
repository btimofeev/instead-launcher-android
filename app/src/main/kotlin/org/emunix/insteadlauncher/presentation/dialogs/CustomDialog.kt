/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.CustomDialogModel
import org.emunix.insteadlauncher.presentation.models.CustomDialogModel.EditTextDialogModel
import org.emunix.insteadlauncher.presentation.models.CustomDialogModel.RadioButtonDialogModel
import org.emunix.insteadlauncher.presentation.models.RadioButtonModel
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@Composable
fun CustomDialog(
    model: CustomDialogModel?,
    onCloseDialog: () -> Unit,
) {
    if (model != null) {
        when (model) {
            is EditTextDialogModel -> EditTextDialog(model, onCloseDialog)
            is RadioButtonDialogModel -> RadioButtonDialog(model, onCloseDialog)
        }
    }
}

@Composable
private fun EditTextDialog(
    model: EditTextDialogModel,
    onCloseDialog: () -> Unit,
) {
    var textInput by remember { mutableStateOf(model.initialText) }
    AlertDialog(
        onDismissRequest = { onCloseDialog() },
        title = { Text(text = model.title) },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = textInput,
                onValueChange = { textInput = it },
                singleLine = true,
                keyboardOptions = if (model.digitsOnly) {
                    KeyboardOptions(keyboardType = KeyboardType.Number)
                } else {
                    KeyboardOptions.Default
                },
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(
                            onClick = { textInput = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear text field"
                            )
                        }
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    model.onTextChanged(textInput)
                    onCloseDialog()
                }
            ) {
                Text(stringResource(R.string.custom_dialog_positive_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onCloseDialog() }
            ) {
                Text(stringResource(R.string.custom_dialog_negative_button))
            }
        }
    )
}

@Composable
private fun RadioButtonDialog(
    model: RadioButtonDialogModel,
    onCloseDialog: () -> Unit,
) {
    var selectedButtonId by remember {
        mutableStateOf(model.buttons.find { it.isChecked }?.id)
    }
    AlertDialog(
        onDismissRequest = { onCloseDialog() },
        title = { Text(text = model.title) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState()))
            {
                model.buttons.forEach { button ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .selectable(
                                selected = (button.id == selectedButtonId),
                                onClick = { selectedButtonId = button.id }
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (button.id == selectedButtonId),
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = button.text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = selectedButtonId
                    if (selected != null) {
                        model.onChoose(selected)
                    }
                    onCloseDialog()
                }
            ) {
                Text(stringResource(R.string.custom_dialog_positive_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onCloseDialog() }
            ) {
                Text(stringResource(R.string.custom_dialog_negative_button))
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun CustomEditTextDialogPreview() {
    InsteadLauncherTheme {
        CustomDialog(
            model = EditTextDialogModel(
                title = "Enter repository URL",
                initialText = "http://instead-games.ru/xml.php",
                onTextChanged = {},
            ),
            onCloseDialog = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun CustomRadioButtonDialogPreview() {
    InsteadLauncherTheme {
        CustomDialog(
            model = RadioButtonDialogModel(
                title = "Application theme",
                buttons = listOf(
                    RadioButtonModel(
                        id = "light",
                        text = "Light",
                        isChecked = false,
                    ),
                    RadioButtonModel(
                        id = "dark",
                        text = "Dart",
                        isChecked = true,
                    ),
                    RadioButtonModel(
                        id = "system",
                        text = "System default",
                        isChecked = false,
                    ),
                ),
                onChoose = {},
            ),
            onCloseDialog = {},
        )
    }
}