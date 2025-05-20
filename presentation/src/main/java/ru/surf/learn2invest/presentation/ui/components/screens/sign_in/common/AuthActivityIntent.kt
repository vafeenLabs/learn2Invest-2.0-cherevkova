package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common

internal sealed class AuthActivityIntent {
    data class AddSymbolToPIN(val symbol: String) : AuthActivityIntent()
    data object RemoveLastSymbolFromPIN : AuthActivityIntent()
    data object ShowFingerPrintDialogForAuth : AuthActivityIntent()
}