package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.refill_account_dialog

internal sealed class RefillAccountDialogIntent {
    data object ClearBalance : RefillAccountDialogIntent()
    data object Refill : RefillAccountDialogIntent()
    data object RemoveLastCharFromBalance: RefillAccountDialogIntent()
    data class AddCharToBalance(val char: String): RefillAccountDialogIntent()
}