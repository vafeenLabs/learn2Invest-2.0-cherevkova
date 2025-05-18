package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.sign_in

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivity

@AndroidEntryPoint
internal class AuthSignInActivity : AuthActivity() {
    override val viewModel: AuthSignInActivityViewModel by viewModels()
}