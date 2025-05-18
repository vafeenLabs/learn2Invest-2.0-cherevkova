package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.delete_profile

internal sealed class DeleteProfileDialogIntent {
    data object DeleteProfile : DeleteProfileDialogIntent()
    data object Dismiss : DeleteProfileDialogIntent()
}