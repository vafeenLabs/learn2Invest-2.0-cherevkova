package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.delete_profile

internal sealed class DeleteProfileDialogEffect {
    data object Dismiss : DeleteProfileDialogEffect()
    data object DismissAndRestartApp : DeleteProfileDialogEffect()
}