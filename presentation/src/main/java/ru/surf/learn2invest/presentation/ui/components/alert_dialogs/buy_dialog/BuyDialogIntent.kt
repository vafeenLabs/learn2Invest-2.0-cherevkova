package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.buy_dialog

internal sealed class BuyDialogIntent {
    data object Buy : BuyDialogIntent()
    data object PlusLot : BuyDialogIntent()
    data object MinusLot : BuyDialogIntent()
    data class SetLot(val lots: Int) : BuyDialogIntent()
    data class SetTradingPassword(val password: String) : BuyDialogIntent()
    data object StopUpdatingPriceFLow : BuyDialogIntent()
    data object SetupAssetIfInDbAndStartUpdatingPriceFLow : BuyDialogIntent()
}