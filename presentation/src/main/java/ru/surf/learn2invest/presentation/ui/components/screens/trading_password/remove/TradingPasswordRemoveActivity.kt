package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.remove

import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivity
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityViewModel
import javax.inject.Inject

/**
 * Активити для удаления торгового пароля.
 *
 * Инициализирует ViewModel с начальными данными и отображает UI для удаления пароля.
 */
@AndroidEntryPoint
internal class TradingPasswordRemoveActivity : TradingPasswordActivity() {
    @Inject
    lateinit var factory: TradingPasswordRemoveActivityViewModel.Factory

    override val viewModel: TradingPasswordActivityViewModel by lazy {
        factory.create(
            TradingPasswordActivityState(
                mainText = this.getString(R.string.remove_trpas),
                mainButtonText = this.getString(R.string.remove),
                passMatchTV = false,
                passwordEditEditText = "",
                passwordConfirmEditText = ""
            )
        )
    }
}
