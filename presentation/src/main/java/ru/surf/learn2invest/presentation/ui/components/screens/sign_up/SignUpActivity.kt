package ru.surf.learn2invest.presentation.ui.components.screens.sign_up

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.ActivitySignUpBinding
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivity
import ru.surf.learn2invest.presentation.utils.setNavigationBarColor
import ru.surf.learn2invest.presentation.utils.setStatusBarColor
import ru.surf.learn2invest.presentation.utils.textListener

/**
 * Activity для регистрации нового пользователя. Обрабатывает ввод имени и фамилии пользователя,
 * их валидацию и обновление данных профиля. После успешной регистрации, пользователь будет
 * перенаправлен на экран входа.
 */
@AndroidEntryPoint
internal class SignUpActivity : AppCompatActivity() {
    private val viewModel: SignUpActivityViewModel by viewModels()

    /**
     * Метод, который вызывается при создании активности. Настроены поля ввода имени и фамилии,
     * а также обработчики событий для кнопки регистрации и действий с клавиатурой.
     *
     * @param savedInstanceState Сохраненное состояние активности, если оно есть.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Настройка цветов навигационной панели и статус-бара
        setNavigationBarColor(window, this, R.color.white, R.color.main_background_dark)
        setStatusBarColor(window, this, R.color.accent_background, R.color.accent_background_dark)

        // Инициализация привязки и отображение UI
        val binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners(binding)
    }

    /**
     * Инициализация слушателей для элементов UI.
     *
     * @param binding Привязка данных к элементам UI.
     */
    private fun initListeners(binding: ActivitySignUpBinding) {
        val state = viewModel.state.value
        setupNameEditText(binding, state.firstName)
        setupLastnameEditText(binding, state.lastName)

        // Обработчик нажатия кнопки "Зарегистрироваться"
        binding.signupBtn.setOnClickListener {
            viewModel.handleIntent(SignUpActivityIntent.SignUp)
        }

        // Обработчики действий для переходов между полями ввода
        binding.inputNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                viewModel.handleIntent(SignUpActivityIntent.OnFirstNameNextClicked)
            }
            false
        }

        binding.inputLastnameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.handleIntent(SignUpActivityIntent.OnLastNameDoneClicked)
            }
            false
        }

        lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                binding.signupBtn.isEnabled = state.isSignUpButtonEnabled

                binding.nameErrorTextView.text = state.firstNameError
                binding.nameErrorTextView.isVisible = state.firstNameError != null

                binding.lastnameErrorTextView.text = state.lastNameError
                binding.lastnameErrorTextView.isVisible = state.lastNameError != null

                applyThemeColors(binding)
            }
        }
        lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    SignUpActivityEffect.FinishActivity -> this@SignUpActivity.finish()
                    SignUpActivityEffect.StartSignInActivitySignUp -> startActivity(
                        AuthActivity.newInstanceSignUP(
                            this@SignUpActivity
                        )
                    )

                    SignUpActivityEffect.OnLastNameRequestFocus -> binding.inputLastnameEditText.requestFocus()
                    SignUpActivityEffect.OnLastNameClearFocus -> binding.inputLastnameEditText.clearFocus()
                    SignUpActivityEffect.OnLastNameHideKeyboard -> binding.inputLastnameEditText.hideKeyboard()
                }
            }
        }
    }

    /**
     * Метод для настройки поля ввода имени пользователя.
     *
     * @param binding Привязка данных к элементам UI.
     * @param firstName Текущее значение имени пользователя.
     */
    private fun setupNameEditText(binding: ActivitySignUpBinding, firstName: String) {
        binding.inputNameEditText.setText(firstName)
        binding.inputNameEditText.addTextChangedListener(textListener(onTextChanged = { s, _, _, _ ->
            viewModel.handleIntent(SignUpActivityIntent.UpdateFirstName(s.toString()))
        }))
        with(binding.inputNameEditText) {
            requestFocus()
            showKeyboard()
        }
    }

    /**
     * Метод для настройки поля ввода фамилии пользователя.
     *
     * @param binding Привязка данных к элементам UI.
     * @param lastName Текущее значение фамилии пользователя.
     */
    private fun setupLastnameEditText(binding: ActivitySignUpBinding, lastName: String) {
        binding.inputLastnameEditText.setText(lastName)
        binding.inputLastnameEditText.addTextChangedListener(
            textListener(onTextChanged = { s, _, _, _ ->
                viewModel.handleIntent(SignUpActivityIntent.UpdateLastName(s.toString()))
            })
        )
    }

    /**
     * Метод для применения цветовой схемы в зависимости от текущей темы.
     *
     * @param binding Привязка данных к элементам UI.
     */
    private fun applyThemeColors(binding: ActivitySignUpBinding) {
        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        binding.signupBtn.backgroundTintList = ColorStateList.valueOf(
            getColor(
                if (binding.signupBtn.isEnabled) {
                    if (isDarkTheme)
                        R.color.accent_background_dark
                    else
                        R.color.accent_background
                } else {
                    if (isDarkTheme)
                        R.color.accent_button_dark
                    else
                        R.color.btn_asset
                }
            )
        )
    }

    /**
     * Скрытие клавиатуры для текущего элемента.
     */
    private fun View.hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Показ клавиатуры для текущего элемента.
     */
    private fun View.showKeyboard() =
        WindowCompat.getInsetsController(window, this).show(WindowInsetsCompat.Type.ime())
}

