package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.hideKeyboard
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.ActivityTradingPasswordBinding
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.change.TradingPasswordChangeActivity
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.create.TradingPasswordCreateActivity
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.remove.TradingPasswordRemoveActivity
import ru.surf.learn2invest.presentation.utils.textListener

/**
 * Базовая Activity для работы с торговым паролем (создание, смена, удаление).
 *
 * Функции:
 * - Создание торгового пароля.
 * - Смена торгового пароля.
 * - Удаление торгового пароля.
 *
 * Наследники должны реализовать собственную ViewModel.
 */
internal abstract class TradingPasswordActivity : AppCompatActivity() {

    /** ViewModel для управления состоянием и логикой экрана торгового пароля */
    protected abstract val viewModel: TradingPasswordActivityViewModel

    /**
     * Инициализация экрана и привязки ViewBinding.
     *
     * @param savedInstanceState Сохраненное состояние Activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTradingPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners(binding)
    }

    /**
     * Настраивает обработчики событий для элементов экрана.
     *
     * @param binding ViewBinding для экрана торгового пароля.
     */
    private fun initListeners(binding: ActivityTradingPasswordBinding) {
        binding.apply {
            arrowBack.setOnClickListener {
                viewModel.handleIntent(TradingPasswordActivityIntent.Back)
            }
            buttonDoTrading.setOnClickListener {
                viewModel.handleIntent(TradingPasswordActivityIntent.Confirm)
            }
            // Слушатели изменений текста для полей ввода паролей
            passwordLast.addTextChangedListener(textListener(onTextChanged = { s, _, _, _ ->
                s?.let {
                    viewModel.handleIntent(TradingPasswordActivityIntent.UpdatePasswordLast(it.toString()))
                }
            }))

            passwordEdit.addTextChangedListener(textListener(onTextChanged = { s, _, _, _ ->
                s?.let {
                    viewModel.handleIntent(TradingPasswordActivityIntent.UpdatePasswordEdit(it.toString()))
                }
            }))

            passwordConfirm.addTextChangedListener(textListener(onTextChanged = { s, _, _, _ ->
                s?.let {
                    viewModel.handleIntent(TradingPasswordActivityIntent.UpdatePasswordConfirm(it.toString()))
                }
            }))

            // Обработка нажатий клавиши "Enter" для перехода между полями
            passwordLast.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    passwordLast.clearFocus()
                    passwordEdit.requestFocus()
                    return@OnKeyListener true
                }
                false
            })

            passwordEdit.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    passwordEdit.clearFocus()
                    passwordConfirm.requestFocus()
                    return@OnKeyListener true
                }
                false
            })

            passwordConfirm.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    passwordConfirm.clearFocus()
                    passwordConfirm.hideKeyboard(activity = this@TradingPasswordActivity)
                    return@OnKeyListener true
                }
                false
            })

            // Подписка на состояние экрана
            lifecycleScope.launchMAIN {
                viewModel.state.collectLatest { state ->
                    header.text = state.mainText

                    minLenTradingPassword.isVisible = state.minLenTradingPasswordTV != null
                    imageMinLenTradingPassword.isVisible = state.minLenTradingPasswordTV != null
                    state.minLenTradingPasswordTV?.let {
                        imageMinLenTradingPassword.setImageDependsOnRule(it)
                    }

                    notMoreThan3.isVisible = state.notMoreThan2TV != null
                    imageNotMoreThan2.isVisible = state.notMoreThan2TV != null
                    state.notMoreThan2TV?.let {
                        imageNotMoreThan2.setImageDependsOnRule(it)
                    }

                    noSeqMoreThan2.isVisible = state.noSeqMoreThan3TV != null
                    imageNoSeqMoreThan2.isVisible = state.noSeqMoreThan3TV != null
                    state.noSeqMoreThan3TV?.let {
                        imageNoSeqMoreThan2.setImageDependsOnRule(it)
                    }

                    passMatch.isVisible = state.passMatchTV != null
                    imagePassMatch.isVisible = state.passMatchTV != null
                    state.passMatchTV?.let {
                        imagePassMatch.setImageDependsOnRule(it)
                    }

                    oldPasCorrect.isVisible = state.oldPasCorrectTV != null
                    imageOldPasCorrect.isVisible = state.oldPasCorrectTV != null
                    state.oldPasCorrectTV?.let {
                        imageOldPasCorrect.setImageDependsOnRule(it)
                    }

                    passwordLastTextInputLayout.isVisible = state.passwordLastEditText != null
                    passwordEditTextInputLayout.isVisible = state.passwordEditEditText != null
                    passwordConfirmTextInputLayout.isVisible = state.passwordConfirmEditText != null

                    buttonDoTrading.isEnabled = state.confirmButtonIsEnabled
                }
            }

            // Подписка на эффекты (например, завершение Activity)
            lifecycleScope.launchMAIN {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        TradingPasswordActivityEffect.Finish -> finish()
                    }
                }
            }
        }
    }

    /**
     * Устанавливает изображение правила в зависимости от его выполнения.
     *
     * @receiver ImageView для отображения статуса правила.
     * @param isOk true, если правило выполнено; false, если нарушено.
     */
    private fun ImageView.setImageDependsOnRule(isOk: Boolean) = setImageDrawable(
        ContextCompat.getDrawable(
            this@TradingPasswordActivity,
            if (isOk) R.drawable.ok_24dp else R.drawable.error_24dp
        )
    )

    companion object {

        /**
         * Создает Intent для экрана создания торгового пароля.
         *
         * @param activity Activity-контекст.
         * @return Intent для TradingPasswordCreateActivity.
         */
        fun newInstanceCreateTP(activity: AppCompatActivity) =
            Intent(activity, TradingPasswordCreateActivity::class.java)

        /**
         * Создает Intent для экрана смены торгового пароля.
         *
         * @param activity Activity-контекст.
         * @return Intent для TradingPasswordChangeActivity.
         */
        fun newInstanceChangeTP(activity: AppCompatActivity) =
            Intent(activity, TradingPasswordChangeActivity::class.java)

        /**
         * Создает Intent для экрана удаления торгового пароля.
         *
         * @param activity Activity-контекст.
         * @return Intent для TradingPasswordRemoveActivity.
         */
        fun newInstanceRemoveTP(activity: AppCompatActivity) =
            Intent(activity, TradingPasswordRemoveActivity::class.java)
    }
}
