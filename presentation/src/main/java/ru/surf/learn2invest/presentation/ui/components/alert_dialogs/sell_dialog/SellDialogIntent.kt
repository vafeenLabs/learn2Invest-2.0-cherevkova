package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.sell_dialog

internal sealed class SellDialogIntent {
    data object Sell : SellDialogIntent()
    data object PlusLot : SellDialogIntent()
    data object MinusLot : SellDialogIntent()
    data class SetLot(val lots: Int) : SellDialogIntent()
    data class SetTradingPassword(val password: String) : SellDialogIntent()
    data object StopUpdatingPriceFLow : SellDialogIntent()
    data object SetupAssetIfInDbAndStartUpdatingPriceFLow : SellDialogIntent()
}