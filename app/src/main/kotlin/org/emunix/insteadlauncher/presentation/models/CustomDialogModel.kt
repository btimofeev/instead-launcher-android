package org.emunix.insteadlauncher.presentation.models

sealed interface CustomDialogModel {

    data class RadioButtonDialogModel(
        val title: String,
        val buttons: List<RadioButtonModel>,
        val onChoose: (radioButonId: String) -> Unit,
    ): CustomDialogModel

    data class EditTextDialogModel(
        val title: String,
        val initialText: String,
        val digitsOnly: Boolean = false,
        val onTextChanged: (text: String) -> Unit,
    ): CustomDialogModel
}