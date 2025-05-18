package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.changing_pin

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivity

@AndroidEntryPoint
internal class AuthChangingPinActivity() : AuthActivity() {
    override val viewModel: AuthChangingPinActivityViewModel by viewModels()
}