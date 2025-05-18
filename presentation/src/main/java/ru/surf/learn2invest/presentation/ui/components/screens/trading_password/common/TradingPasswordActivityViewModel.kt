package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.utils.launchIO

/**
 * Абстрактный ViewModel для управления логикой экрана работы с торговым паролем.
 *
 * Обрабатывает ввод паролей, обновляет состояние и управляет эффектами UI.
 *
 * @property initialState Начальное состояние экрана торгового пароля.
 */
internal abstract class TradingPasswordActivityViewModel(
    initialState: TradingPasswordActivityState,
) : ViewModel() {

    /** Минимальная длина торгового пароля */
    protected val minPasswordLength = 6

    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected val _effects = MutableSharedFlow<TradingPasswordActivityEffect>()
    val effects = _effects.asSharedFlow()

    /**
     * Обрабатывает интенты, поступающие из UI.
     *
     * @param intent Действие пользователя.
     */
    fun handleIntent(intent: TradingPasswordActivityIntent) {
        viewModelScope.launchIO {
            when (intent) {
                is TradingPasswordActivityIntent.UpdatePasswordLast ->
                    updatePasswordLast(intent.passwordLast)

                is TradingPasswordActivityIntent.UpdatePasswordEdit ->
                    updatePasswordEdit(intent.passwordEdit)

                is TradingPasswordActivityIntent.UpdatePasswordConfirm ->
                    updatePasswordConfirm(intent.passwordConfirm)

                TradingPasswordActivityIntent.Confirm -> confirmAndFinish()
                TradingPasswordActivityIntent.Back -> back()
            }
        }
    }

    /**
     * Выполняет подтверждение пароля и завершает экран.
     */
    private suspend fun confirmAndFinish() {
        confirm()
        _effects.emit(TradingPasswordActivityEffect.Finish)
    }

    /**
     * Абстрактный метод для подтверждения действия (например, проверки и сохранения пароля).
     * Реализуется в наследниках.
     */
    abstract suspend fun confirm()

    /**
     * Абстрактный метод для конфигурации правил валидации пароля.
     * Принимает текущее состояние и возвращает обновлённое с учётом правил.
     *
     * @param state Текущее состояние экрана.
     * @return Обновлённое состояние с учетом правил.
     */
    abstract fun configureRules(state: TradingPasswordActivityState): TradingPasswordActivityState

    /**
     * Обновляет значение поля "старый пароль" и применяет правила валидации.
     *
     * @param password Новое значение старого пароля.
     */
    private fun updatePasswordLast(password: String) {
        _state.update { it.copy(passwordLastEditText = password) }
        _state.update { configureRules(it) }
    }

    /**
     * Обрабатывает действие возврата (например, закрытие экрана).
     */
    private suspend fun back() {
        _effects.emit(TradingPasswordActivityEffect.Finish)
    }

    /**
     * Обновляет значение поля "новый пароль" и применяет правила валидации.
     *
     * @param password Новое значение нового пароля.
     */
    private fun updatePasswordEdit(password: String) {
        _state.update { it.copy(passwordEditEditText = password) }
        _state.update { configureRules(it) }
    }

    /**
     * Обновляет значение поля "подтверждение пароля" и применяет правила валидации.
     *
     * @param password Новое значение подтверждения пароля.
     */
    private fun updatePasswordConfirm(password: String) {
        _state.update { it.copy(passwordConfirmEditText = password) }
        _state.update { configureRules(it) }
    }
}
