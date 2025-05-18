package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.sign_up

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivity

@AndroidEntryPoint
internal class AuthSignUpActivity : AuthActivity() {
    override val viewModel: AuthSignUpActivityViewModel by viewModels()
}