package ru.surf.learn2invest.presentation.ui.components.screens.sign_up

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.domain_models.Settings
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.presentation.R
import javax.inject.Inject

/**
 * ViewModel для экрана регистрации. Обрабатывает логику обновления имени и фамилии пользователя,
 * а также валидацию введенных данных.
 *
 * @property settingsManager Менеджер профиля для обновления данных пользователя.
 * @property context Контекст приложения, используется для получения строк ресурсов.
 */
@HiltViewModel
internal class SignUpActivityViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    /** Состояние экрана регистрации, доступно для наблюдения */
    private val _state = MutableStateFlow(SignUpActivityState(firstName = "", lastName = ""))
    val state = _state.asStateFlow()

    /** Поток эффектов для управления UI (навигация, фокус, клавиатура) */
    private val _effects = MutableSharedFlow<SignUpActivityEffect>()
    val effects = _effects.asSharedFlow()

    /** Максимальная длина для имени и фамилии */
    private val lengthLimit = 24

    /**
     * Обрабатывает интенты, поступающие из UI.
     *
     * @param intent Интент, описывающий действие пользователя.
     */
    fun handleIntent(intent: SignUpActivityIntent) {
        viewModelScope.launchIO {
            when (intent) {
                is SignUpActivityIntent.UpdateFirstName -> updateFirstname(intent.firstName)
                is SignUpActivityIntent.UpdateLastName -> updateLastName(intent.lastName)
                SignUpActivityIntent.SignUp -> signUp()
                SignUpActivityIntent.OnFirstNameNextClicked -> onFirstNameNexClicked()
                SignUpActivityIntent.OnLastNameDoneClicked -> onLastNameDoneCLicked()
            }
        }
    }

    /**
     * Обновляет профиль пользователя, применяя функцию обновления.
     *
     * @param updating Функция, которая принимает текущий профиль и возвращает обновленный.
     */
    private suspend fun updateProfile(updating: (Settings) -> Settings) {
        settingsManager.update(updating)
    }

    /**
     * Обновляет имя пользователя в состоянии и запускает его валидацию.
     *
     * @param firstName Новое имя пользователя.
     */
    private fun updateFirstname(firstName: String) {
        _state.update { it.copy(firstName = firstName) }
        validateFirstname(firstName)
        updateSignUpButtonState()
    }

    /**
     * Обновляет фамилию пользователя в состоянии и запускает ее валидацию.
     *
     * @param lastName Новая фамилия пользователя.
     */
    private fun updateLastName(lastName: String) {
        _state.update { it.copy(lastName = lastName) }
        validateLastname(lastName)
        updateSignUpButtonState()
    }

    /**
     * Валидирует имя пользователя.
     *
     * @param firstname Имя для проверки.
     * @return true, если имя валидно, иначе false.
     */
    private fun validateFirstname(firstname: String): Boolean = when {
        firstname.isEmpty() -> {
            _state.update { it.copy(firstNameError = null) }
            false
        }

        firstname.trim() != firstname -> {
            _state.update { it.copy(firstNameError = context.getString(R.string.contains_spaces)) }
            false
        }

        firstname.length > lengthLimit -> {
            _state.update { it.copy(firstNameError = context.getString(R.string.limit_len_exceeded)) }
            false
        }

        else -> {
            _state.update { it.copy(firstNameError = null) }
            true
        }
    }

    /**
     * Валидирует фамилию пользователя.
     *
     * @param lastName Фамилия для проверки.
     * @return true, если фамилия валидна, иначе false.
     */
    private fun validateLastname(lastName: String): Boolean = when {
        lastName.isEmpty() -> {
            _state.update { it.copy(lastNameError = null) }
            false
        }

        lastName.trim() != lastName -> {
            _state.update { it.copy(lastNameError = context.getString(R.string.contains_spaces)) }
            false
        }

        lastName.length > lengthLimit -> {
            _state.update { it.copy(lastNameError = context.getString(R.string.limit_len_exceeded)) }
            false
        }

        else -> {
            _state.update { it.copy(lastNameError = null) }
            true
        }
    }

    /**
     * Выполняет регистрацию: обновляет профиль и инициирует переход на экран входа.
     */
    private suspend fun signUp() {
        val state = _state.value
        updateProfile {
            it.copy(
                firstName = state.firstName, lastName = state.lastName
            )
        }
        _effects.emit(SignUpActivityEffect.StartSignInActivitySignUp)
        _effects.emit(SignUpActivityEffect.FinishActivity)
    }

    /**
     * Обрабатывает нажатие кнопки "Далее" в поле имени.
     * Проверяет заполненность и устанавливает фокус на поле фамилии.
     */
    private suspend fun onFirstNameNexClicked() {
        val firstName = _state.value.firstName
        _state.update {
            it.copy(
                firstNameError = if (firstName.isEmpty()) {
                    context.getString(R.string.empty_error)
                } else null
            )
        }
        _effects.emit(SignUpActivityEffect.OnLastNameRequestFocus)
    }

    /**
     * Обрабатывает нажатие кнопки "Готово" в поле фамилии.
     * Проверяет заполненность, скрывает клавиатуру и снимает фокус.
     */
    private suspend fun onLastNameDoneCLicked() {
        val lastName = _state.value.lastName
        _state.update {
            it.copy(
                lastNameError = if (lastName.isEmpty()) {
                    context.getString(R.string.empty_error)
                } else null
            )
        }
        if (lastName.isNotEmpty()) {
            _effects.emit(SignUpActivityEffect.OnLastNameHideKeyboard)
            _effects.emit(SignUpActivityEffect.OnLastNameClearFocus)
        }
    }

    /**
     * Обновляет состояние кнопки регистрации в зависимости от валидности полей.
     */
    private fun updateSignUpButtonState() {
        _state.update {
            it.copy(
                isSignUpButtonEnabled =
                    it.firstName.isNotEmpty() &&
                            it.firstNameError == null &&
                            it.lastName.isNotEmpty() &&
                            it.lastNameError == null
            )
        }
    }
}
