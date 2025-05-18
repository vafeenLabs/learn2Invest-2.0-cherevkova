package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.create

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.surf.learn2invest.domain.cryptography.usecase.UpdateTradingPasswordUseCase
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityViewModel
import ru.surf.learn2invest.presentation.utils.isThisContains3NumbersIfIsNotEmpty
import ru.surf.learn2invest.presentation.utils.isThisContainsSequenceIfIsNotEmpty

/**
 * ViewModel для экрана создания торгового пароля.
 *
 * Обрабатывает логику валидации и обновления торгового пароля.
 *
 * @property updateTradingPasswordUseCase UseCase для обновления торгового пароля.
 * @param initialState Начальное состояние экрана.
 */
internal class TradingPasswordCreateActivityViewModel @AssistedInject constructor(
    @Assisted initialState: TradingPasswordActivityState,
    private val updateTradingPasswordUseCase: UpdateTradingPasswordUseCase,
) : TradingPasswordActivityViewModel(initialState) {

    /**
     * Подтверждает и сохраняет торговый пароль, вызывая соответствующий UseCase.
     */
    override suspend fun confirm() {
        _state.value.passwordConfirmEditText?.let { updateTradingPasswordUseCase.invoke(it) }
    }

    /**
     * Конфигурирует правила валидации для торгового пароля.
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
            passMatchTV = state.passwordEditEditText?.isNotEmpty() == true && state.passwordEditEditText == state.passwordConfirmEditText
        )

    /**
     * Factory для создания экземпляров ViewModel с параметрами.
     */
    @AssistedFactory
    interface Factory {
        fun create(initialState: TradingPasswordActivityState): TradingPasswordCreateActivityViewModel
    }
}
