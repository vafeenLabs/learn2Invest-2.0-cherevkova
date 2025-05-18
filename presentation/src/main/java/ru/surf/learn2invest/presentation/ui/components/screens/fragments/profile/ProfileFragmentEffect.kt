package ru.surf.learn2invest.presentation.ui.components.screens.fragments.profile

internal sealed class ProfileFragmentEffect {
    data object ShowDeleteProfileDialogEffect : ProfileFragmentEffect()
    data object ShowResetStatsDialogEffect : ProfileFragmentEffect()
    data object TradingPasswordActivityChangeTP : ProfileFragmentEffect()
    data object TradingPasswordActivityCreateTP : ProfileFragmentEffect()
    data object TradingPasswordActivityRemoveTP : ProfileFragmentEffect()
    data object SignInActivityChangingPIN : ProfileFragmentEffect()
    data class FingerPrintBottomSheet(
        val onSuccess: () -> Unit = {},
        val onError: () -> Unit = {},
        val onCancel: () -> Unit = {},
    ) : ProfileFragmentEffect()
}