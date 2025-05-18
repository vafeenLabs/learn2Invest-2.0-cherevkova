package ru.surf.learn2invest.presentation.ui.components.screens.fragments.profile

/**
 * Состояние экрана профиля пользователя.
 *
 * @property firstName Имя пользователя.
 * @property lastName Фамилия пользователя.
 * @property tradingPasswordHash Хэш торгового пароля пользователя (если установлен).
 * @property biometry Флаг, указывающий, включена ли биометрическая аутентификация.
 * @property isBiometryAvailable Флаг, указывающий, доступна ли биометрия на устройстве.
 */
internal data class ProfileFragmentState(
    val firstName: String,
    val lastName: String,
    val tradingPasswordHash: String? = null,
    val biometry: Boolean = false,
    val isBiometryAvailable: Boolean = false,
)
