package ru.surf.learn2invest.presentation.ui.components.screens.sign_up


/**
 * Класс для представления состояния экрана регистрации.
 *
 * @property firstName Имя пользователя.
 * @property firstNameError Сообщение об ошибке для поля имени, если есть.
 * @property lastName Фамилия пользователя.
 * @property lastNameError Сообщение об ошибке для поля фамилии, если есть.
 * @property isSignUpButtonEnabled Флаг, определяющий доступность кнопки регистрации.
 */
internal data class SignUpActivityState(
    val firstName: String,
    val firstNameError: String? = null,
    val lastName: String,
    val lastNameError: String? = null,
    val isSignUpButtonEnabled: Boolean = false,
)
