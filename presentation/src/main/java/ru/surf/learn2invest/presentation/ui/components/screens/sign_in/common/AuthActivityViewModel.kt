package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.cryptography.usecase.VerifyPINUseCase
import ru.surf.learn2invest.domain.services.ProfileManager
import ru.surf.learn2invest.domain.utils.launchIO

/**
 * Абстрактная ViewModel для управления логикой экранов аутентификации по PIN-коду.
 *
 * @param initialState Начальное состояние экрана.
 * @param verifyPINUseCase UseCase для проверки корректности PIN-кода.
 * @param profileManager Менеджер профиля пользователя.
 */
internal abstract class AuthActivityViewModel(
    initialState: AuthActivityState,
    protected val verifyPINUseCase: VerifyPINUseCase,
    protected val profileManager: ProfileManager,
) : ViewModel() {
    /** Поток данных профиля пользователя */
    protected val profileFlow = profileManager.profileFlow

    /** Состояние экрана аутентификации */
    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    /** Поток side-эффектов для управления UI */
    protected val _effects = MutableSharedFlow<AuthActivityEffect>()
    val effects = _effects.asSharedFlow()

    /**
     * Обрабатывает пользовательские интенты.
     *
     * @param intent Интент, описывающий действие пользователя (добавление/удаление символа PIN и т.д.).
     */
    fun handleIntent(intent: AuthActivityIntent) {
        viewModelScope.launchIO {
            when (intent) {
                is AuthActivityIntent.AddSymbolToPIN -> addSymbolToPin(intent.symbol)
                AuthActivityIntent.RemoveLastSymbolFromPIN -> removeLastSymbolFromPIN()
            }
        }
    }

    /**
     * Добавляет символ к текущему PIN-коду и обновляет состояние точек.
     * Если PIN-код достиг длины 4 символа - блокирует клавиатуру и вызывает onPinIsFull().
     *
     * @param symbol Символ для добавления.
     */
    private suspend fun addSymbolToPin(symbol: String) {
        _state.update { it.copy(pin = if (it.pin.length < 4) "${it.pin}${symbol}" else it.pin) }
        paintDotsDependsOnPIN()
        val state = _state.value
        if (state.pin.length == 4) {
            blockKeyBoard()
            onPinIsFull()
        }
    }

    /**
     * Удаляет последний символ из PIN-кода и обновляет состояние точек.
     */
    private fun removeLastSymbolFromPIN() {
        _state.update { it.copy(pin = it.pin.dropLast(1)) }
        paintDotsDependsOnPIN()
    }

    /**
     * Обновляет состояние точек в зависимости от количества введённых символов PIN-кода.
     */
    protected fun paintDotsDependsOnPIN() {
        _state.update {
            it.copy(
                dots = AuthDotsState(
                    one = fULLOrNULL(it.pin, 1),
                    two = fULLOrNULL(it.pin, 2),
                    three = fULLOrNULL(it.pin, 3),
                    four = fULLOrNULL(it.pin, 4)
                )
            )
        }
    }

    /**
     * Возвращает состояние точки в зависимости от длины введённого PIN-кода.
     *
     * @param pin Текущий PIN-код.
     * @param length Позиция точки.
     * @return AuthDotState.FULL если длина PIN-кода >= позиции, иначе AuthDotState.NULL.
     */
    protected fun fULLOrNULL(pin: String, length: Int) =
        if (pin.length >= length) AuthDotState.FULL else AuthDotState.NULL

    /**
     * Блокирует клавиатуру, запрещая дальнейший ввод.
     */
    protected suspend fun blockKeyBoard() =
        _effects.emit(AuthActivityEffect.ChangeEnabledKeyboardState(false))

    /**
     * Разблокирует клавиатуру, разрешая ввод.
     */
    protected suspend fun unblockKeyBoard() =
        _effects.emit(AuthActivityEffect.ChangeEnabledKeyboardState(true))

    /**
     * Абстрактный метод, вызывается когда введён полный PIN-код (4 символа).
     */
    protected abstract suspend fun onPinIsFull()

    /**
     * Возвращает состояние всех точек "пусто".
     */
    protected fun allWhiteDots(): AuthDotsState<AuthDotState> = AuthDotsState(
        one = AuthDotState.NULL,
        two = AuthDotState.NULL,
        three = AuthDotState.NULL,
        four = AuthDotState.NULL,
    )

    /**
     * Проверяет корректность введённого PIN-кода.
     *
     * @return true, если PIN-код верный, иначе false.
     */
    fun verifyPIN(): Boolean = verifyPINUseCase.invoke(profileFlow.value, _state.value.pin)
}
