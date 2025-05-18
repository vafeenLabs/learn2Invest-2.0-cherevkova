package ru.surf.learn2invest.presentation.ui.components.screens.sign_up

internal sealed class SignUpActivityEffect {
    data object StartSignInActivitySignUp : SignUpActivityEffect()
    data object FinishActivity : SignUpActivityEffect()
    data object OnLastNameRequestFocus : SignUpActivityEffect()
    data object OnLastNameHideKeyboard : SignUpActivityEffect()
    data object OnLastNameClearFocus : SignUpActivityEffect()
}