package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.create

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.surf.learn2invest.domain.cryptography.usecase.UpdateTradingPasswordUseCase
import ru.surf.learn2invest.domain.cryptography.usecase.VerifyTradingPasswordUseCase
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityViewModel
import ru.surf.learn2invest.presentation.utils.isThisContains3NumbersIfIsNotEmpty
import ru.surf.learn2invest.presentation.utils.isThisContainsSequenceIfIsNotEmpty

/**
 * ViewModel для экрана смены торгового пароля.
 *
 * Обрабатывает валидацию и обновление пароля, включая проверку старого пароля.
 *
 * @property verifyTradingPasswordUseCase UseCase для проверки корректности старого пароля.
 * @property updateTradingPasswordUseCase UseCase для обновления пароля.
 * @param initialState Начальное состояние экрана.
 */
internal class TradingPasswordChangeActivityViewModel @AssistedInject constructor(
    @Assisted initialState: TradingPasswordActivityState,
    private val verifyTradingPasswordUseCase: VerifyTradingPasswordUseCase,
    private val updateTradingPasswordUseCase: UpdateTradingPasswordUseCase,
) : TradingPasswordActivityViewModel(initialState) {

    /**
     * Подтверждает смену пароля, вызывая обновление с новым паролем.
     */
    override suspend fun confirm() {
        _state.value.passwordConfirmEditText?.let { updateTradingPasswordUseCase.invoke(it) }
    }

    /**
     * Конфигурирует правила валидации для смены торгового пароля.
     *
     * @param state Текущее состояние экрана.
     * @return Обновленное состояние с результатами проверки правил.
     */
    override fun configureRules(state: TradingPasswordActivityState): TradingPasswordActivityState =
        state.copy(
            minLenTradingPasswordTV =
                state.passwordEditEditText?.let { pass -> pass.length >= minPasswordLength },
            notMoreThan2TV =
                state.passwordEditEditText?.isThisContains3NumbersIfIsNotEmpty()?.not(),
            noSeqMoreThan3TV =
                state.passwordEditEditText?.isThisContainsSequenceIfIsNotEmpty()?.not(),
            passMatchTV =
                state.passwordEditEditText?.isNotEmpty() == true && state.passwordEditEditText == state.passwordConfirmEditText,
            oldPasCorrectTV =
                state.passwordLastEditText?.let { tradingPassword ->
                    verifyTradingPasswordUseCase.invoke(tradingPassword)
                },
        )

    /**
     * Factory для создания экземпляров ViewModel с параметрами.
     */
    @AssistedFactory
    interface Factory {
        fun create(initialState: TradingPasswordActivityState): TradingPasswordChangeActivityViewModel
    }
}
