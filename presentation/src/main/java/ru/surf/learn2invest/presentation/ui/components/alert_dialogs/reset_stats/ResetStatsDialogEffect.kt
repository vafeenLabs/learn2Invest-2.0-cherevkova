package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.reset_stats

internal sealed class ResetStatsDialogEffect {
    data object Dismiss : ResetStatsDialogEffect()
    data object ToastResetStateSuccessful : ResetStatsDialogEffect()
}