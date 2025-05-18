package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common

internal sealed class TradingPasswordActivityIntent {
    data class UpdatePasswordLast(val passwordLast: String) : TradingPasswordActivityIntent()
    data class UpdatePasswordEdit(val passwordEdit: String) : TradingPasswordActivityIntent()
    data class UpdatePasswordConfirm(val passwordConfirm: String) : TradingPasswordActivityIntent()
    data object Confirm : TradingPasswordActivityIntent()
    data object Back : TradingPasswordActivityIntent()
}