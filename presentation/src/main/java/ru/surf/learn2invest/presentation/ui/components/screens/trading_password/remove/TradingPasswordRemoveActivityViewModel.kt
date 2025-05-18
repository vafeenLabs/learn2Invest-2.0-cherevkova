package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.remove

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.surf.learn2invest.domain.cryptography.usecase.RemoveTradingPasswordUseCase
import ru.surf.learn2invest.domain.cryptography.usecase.VerifyTradingPasswordUseCase
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivityViewModel

/**
 * ViewModel для экрана удаления торгового пароля.
 *
 * Обрабатывает проверку пароля и удаление торгового пароля.
 *
 * @property verifyTradingPasswordUseCase UseCase для проверки корректности введённого пароля.
 * @property removeTradingPasswordUseCase UseCase для удаления пароля.
 * @param initialState Начальное состояние экрана.
 */
internal class TradingPasswordRemoveActivityViewModel @AssistedInject constructor(
    @Assisted initialState: TradingPasswordActivityState,
    private val verifyTradingPasswordUseCase: VerifyTradingPasswordUseCase,
    private val removeTradingPasswordUseCase: RemoveTradingPasswordUseCase,
) : TradingPasswordActivityViewModel(initialState) {

    /**
     * Подтверждает удаление торгового пароля.
     */
    override suspend fun confirm() {
        removeTradingPasswordUseCase.invoke()
    }

    /**
     * Конфигурирует правила валидации для удаления пароля.
     *
     * @param state Текущее состояние экрана.
     * @return Обновлённое состояние с результатом проверки совпадения паролей и их корректности.
     */
    override fun configureRules(state: TradingPasswordActivityState): TradingPasswordActivityState =
        state.copy(
            passMatchTV = state.passwordEditEditText != null &&
                    state.passwordConfirmEditText != null &&
                    state.passwordEditEditText == state.passwordConfirmEditText &&
                    verifyTradingPasswordUseCase.invoke(state.passwordEditEditText)
        )

    /**
     * Factory для создания экземпляров ViewModel с параметрами.
     */
    @AssistedFactory
    interface Factory {
        fun create(initialState: TradingPasswordActivityState): TradingPasswordRemoveActivityViewModel
    }
}
