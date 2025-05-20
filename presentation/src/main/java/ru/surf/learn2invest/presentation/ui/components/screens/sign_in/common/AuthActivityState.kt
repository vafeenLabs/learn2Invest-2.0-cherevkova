package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common

/**
 * Состояние экрана аутентификации по PIN-коду.
 *
 * @property mainText Основной текст для отображения на экране (например, подсказка или инструкция).
 * @property pin Текущий введённый пользователем PIN-код.
 * @property dots Состояние точек, отображающих прогресс ввода PIN-кода.
 */
internal data class AuthActivityState(
    val mainText: String,
    val pin: String = "",
    val isFingerprintButtonShowed: Boolean,
    val dots: AuthDotsState<AuthDotState> = AuthDotsState(
        one = AuthDotState.NULL,
        two = AuthDotState.NULL,
        three = AuthDotState.NULL,
        four = AuthDotState.NULL,
    ),
)
