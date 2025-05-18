package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common

import android.widget.ImageView

internal sealed class AuthActivityEffect {
    data object NavigateToMainScreen : AuthActivityEffect()
    data object Finish : AuthActivityEffect()
    data class ChangeEnabledKeyboardState(val isEnabled: Boolean) : AuthActivityEffect()
    data class AnimatePinDots(val animate: (ImageView, ImageView, ImageView, ImageView) -> Unit) :
        AuthActivityEffect()

    data class FingerPrintBottomSheet(
        val onSuccess: () -> Unit = {},
        val onError: () -> Unit = {},
        val onCancel: () -> Unit = {},
    ) :
        AuthActivityEffect()
}