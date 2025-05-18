package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.reset_stats

internal sealed class ResetStatsDialogIntent {
    data object ResetStats : ResetStatsDialogIntent()
    data object Dismiss : ResetStatsDialogIntent()
}