package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.create

import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivity
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityViewModel
import javax.inject.Inject

/**
 * Активити для создания торгового пароля.
 *
 * Инициализирует ViewModel с начальными данными и отображает UI для создания пароля.
 */
@AndroidEntryPoint
internal class TradingPasswordCreateActivity : TradingPasswordActivity() {
    @Inject
    lateinit var factory: TradingPasswordCreateActivityViewModel.Factory

    override val viewModel: TradingPasswordActivityViewModel by lazy {
        factory.create(
            TradingPasswordActivityState(
                mainText = this.getString(R.string.create_trpas),
                mainButtonText = this.getString(R.string.create),
                minLenTradingPasswordTV = false,
                notMoreThan2TV = false,
                noSeqMoreThan3TV = false,
                passMatchTV = false,
                passwordEditEditText = "",
                passwordConfirmEditText = ""
            )
        )
    }
}
