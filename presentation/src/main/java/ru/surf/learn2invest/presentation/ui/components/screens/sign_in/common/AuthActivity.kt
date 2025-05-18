package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.cryptography.FingerprintAuthenticator
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.domain.utils.tapOn
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.ActivitySignInBinding
import ru.surf.learn2invest.presentation.ui.components.screens.host.HostActivity
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.changing_pin.AuthChangingPinActivity
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.sign_in.AuthSignInActivity
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.sign_up.AuthSignUpActivity
import ru.surf.learn2invest.presentation.utils.setNavigationBarColor
import ru.surf.learn2invest.presentation.utils.setStatusBarColor
import javax.inject.Inject

/**
 * Абстрактная Activity для аутентификации пользователя с помощью PIN-кода и биометрии.
 *
 * Реализует общую логику для экранов входа, регистрации и смены PIN-кода:
 * - Обработка нажатий цифровых кнопок и backspace.
 * - Отображение состояния PIN-кода с помощью точек.
 * - Реализация side-эффектов через эффекты viewModel.
 * - Интеграция с FingerprintAuthenticator для биометрии.
 *
 * Наследники должны предоставить свою реализацию viewModel.
 */
internal abstract class AuthActivity : AppCompatActivity() {
    /** ViewModel, реализующий бизнес-логику для экрана аутентификации */
    protected abstract val viewModel: AuthActivityViewModel

    /** Аутентификатор отпечатка пальца, внедряется через Hilt */
    @Inject
    lateinit var fingerprintAuthenticator: FingerprintAuthenticator

    /**
     * Вызывается при создании Activity. Настраивает цвета системы, инициализирует ViewBinding и слушатели.
     *
     * @param savedInstanceState Сохраненное состояние Activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarColor(window, this, R.color.accent_background, R.color.accent_background_dark)
        setNavigationBarColor(
            window, this, R.color.accent_background, R.color.accent_background_dark
        )

        val binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners(binding)
    }

    /**
     * Инициализирует слушатели для кнопок, backspace и подписку на состояния/эффекты viewModel.
     *
     * @param binding ViewBinding для экрана аутентификации.
     */
    private fun initListeners(binding: ActivitySignInBinding) {
        binding.apply {
            val numberButtons = listOf(
                button0,
                button1,
                button2,
                button3,
                button4,
                button5,
                button6,
                button7,
                button8,
                button9,
            )

            // Обработка нажатия цифровых кнопок
            for (index in 0..numberButtons.lastIndex) {
                numberButtons[index].setOnClickListener {
                    viewModel.handleIntent(AuthActivityIntent.AddSymbolToPIN("$index"))
                    it.tapOn() // TODO move into side effects
                }
            }

            // Обработка нажатия backspace
            backspace.setOnClickListener {
                viewModel.handleIntent(AuthActivityIntent.RemoveLastSymbolFromPIN)
            }

            // Подписка на изменения состояния PIN-кода
            lifecycleScope.launchMAIN {
                viewModel.state.collectLatest { state ->
                    enterPin.text = state.mainText
                    dot1.drawable.paintDotDependsOnState(state.dots.one)
                    dot2.drawable.paintDotDependsOnState(state.dots.two)
                    dot3.drawable.paintDotDependsOnState(state.dots.three)
                    dot4.drawable.paintDotDependsOnState(state.dots.four)
                }
            }
            // Подписка на эффекты (side-effects)
            lifecycleScope.launchMAIN {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        AuthActivityEffect.NavigateToMainScreen -> startAuthActivity()

                        AuthActivityEffect.Finish -> finishThis()
                        is AuthActivityEffect.ChangeEnabledKeyboardState -> {
                            (numberButtons + backspace).forEach {
                                it.isEnabled = effect.isEnabled
                            }
                        }

                        is AuthActivityEffect.AnimatePinDots -> effect.animate(
                            binding.dot1,
                            binding.dot2,
                            binding.dot3,
                            binding.dot4
                        )

                        is AuthActivityEffect.FingerPrintBottomSheet -> {
                            fingerprintAuthenticator.setDesignBottomSheet(
                                title = getString(R.string.biometry),
                                cancelText = getString(R.string.cancel),
                            ).setSuccessCallback {
                                effect.onSuccess()
                            }.setCancelCallback {
                                effect.onCancel()
                            }.setFailedCallback {
                                effect.onError()
                            }.auth(this@AuthActivity)
                        }
                    }
                }
            }
        }
    }

    /**
     * Изменяет цвет точки в зависимости от состояния ввода PIN-кода.
     *
     * @receiver Drawable точки.
     * @param state Состояние точки (NULL, FULL, RIGHT, ERROR).
     */
    private fun Drawable.paintDotDependsOnState(state: AuthDotState) = setTint(
        when (state) {
            AuthDotState.NULL -> Color.WHITE
            AuthDotState.FULL -> Color.BLACK
            AuthDotState.RIGHT -> Color.GREEN
            AuthDotState.ERROR -> Color.RED
        }
    )

    /**
     * Запускает основную Activity приложения после успешной аутентификации.
     */
    private fun startAuthActivity() {
        startActivity(
            Intent(
                this@AuthActivity,
                HostActivity::class.java
            )
        )
    }

    /**
     * Завершает текущую Activity.
     */
    private fun finishThis() {
        this@AuthActivity.finish()
    }

    companion object {
        /**
         * Создает Intent для запуска экрана входа по PIN-коду.
         *
         * @param activity Activity-контекст.
         * @return Intent для AuthSignInActivity.
         */
        fun newInstanceSignIN(activity: AppCompatActivity) =
            Intent(activity, AuthSignInActivity::class.java)

        /**
         * Создает Intent для запуска экрана регистрации по PIN-коду.
         *
         * @param activity Activity-контекст.
         * @return Intent для AuthSignUpActivity.
         */
        fun newInstanceSignUP(activity: AppCompatActivity) =
            Intent(activity, AuthSignUpActivity::class.java)

        /**
         * Создает Intent для запуска экрана смены PIN-кода.
         *
         * @param activity Activity-контекст.
         * @return Intent для AuthChangingPinActivity.
         */
        fun newInstanceChangingPIN(activity: AppCompatActivity) =
            Intent(activity, AuthChangingPinActivity::class.java)
    }
}
