package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.change

import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivity
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityViewModel
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.create.TradingPasswordChangeActivityViewModel
import javax.inject.Inject

/**
 * Активити для смены торгового пароля.
 *
 * Инициализирует ViewModel с начальными значениями и отображает UI для смены пароля.
 */
@AndroidEntryPoint
internal class TradingPasswordChangeActivity : TradingPasswordActivity() {
    @Inject
    lateinit var factory: TradingPasswordChangeActivityViewModel.Factory

    override val viewModel: TradingPasswordActivityViewModel by lazy {
        factory.create(
            TradingPasswordActivityState(
                mainText = this.getString(R.string.change_trpas),
                mainButtonText = this.getString(R.string.change),
                minLenTradingPasswordTV = false,
                notMoreThan2TV = false,
                noSeqMoreThan3TV = false,
                passMatchTV = false,
                oldPasCorrectTV = false,
                passwordLastEditText = "",
                passwordEditEditText = "",
                passwordConfirmEditText = ""
            )
        )
    }
}
