package ru.surf.learn2invest.presentation.ui.components.screens.sign_up

internal sealed class SignUpActivityIntent {
    data class UpdateFirstName(val firstName: String) : SignUpActivityIntent()
    data class UpdateLastName(val lastName: String) : SignUpActivityIntent()
    data object SignUp : SignUpActivityIntent()
    data object OnFirstNameNextClicked : SignUpActivityIntent()
    data object OnLastNameDoneClicked : SignUpActivityIntent()
}