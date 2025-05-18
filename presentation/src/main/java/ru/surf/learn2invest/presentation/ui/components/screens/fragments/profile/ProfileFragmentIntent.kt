package ru.surf.learn2invest.presentation.ui.components.screens.fragments.profile

internal sealed class ProfileFragmentIntent {
    data object IsBiometricAvailable : ProfileFragmentIntent()
    data object ShowDeleteProfileDialogEffect : ProfileFragmentIntent()
    data object ShowResetStatsDialogEffect : ProfileFragmentIntent()
    data object BiometryBtnSwitch : ProfileFragmentIntent()
    data object ChangeTradingPassword : ProfileFragmentIntent()
    data object ChangeTransactionConfirmation : ProfileFragmentIntent()
    data object ChangePIN : ProfileFragmentIntent()
}