package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.sign_in

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.animator.usecase.AnimateDotsUseCase
import ru.surf.learn2invest.domain.cryptography.usecase.VerifyPINUseCase
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivityEffect
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivityViewModel
import javax.inject.Inject

/**
 * ViewModel для экрана входа по PIN-коду.
 *
 * Обрабатывает ввод PIN-кода, проверяет корректность и анимирует точки ввода.
 * При успешной проверке инициирует переход на главный экран.
 *
 * @property context Контекст приложения для доступа к ресурсам.
 * @property animateDotsUseCase UseCase для анимации точек PIN.
 */
@HiltViewModel
internal class AuthSignInActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsManager: SettingsManager,
    verifyPINUseCase: VerifyPINUseCase,
    animateDotsUseCase: AnimateDotsUseCase,
) : AuthActivityViewModel(
    initialState = AuthActivityState(
        mainText = context.getString(R.string.enter_pin_str),
        isFingerprintButtonShowed = true
    ),
    settingsManager = settingsManager,
    verifyPINUseCase = verifyPINUseCase,
    animateDotsUseCase = animateDotsUseCase
) {
    init {
        viewModelScope.launchIO {
            showFingerPrintBottomSheetForAuth()
        }
    }

    /**
     * Обрабатывает событие полного ввода PIN-кода.
     * Проверяет PIN, запускает анимацию точек и инициирует навигацию или сброс ввода.
     */
    override suspend fun onPinIsFull() {
        delay(300)
        if (verifyPIN()) {
            _effects.emit(AuthActivityEffect.AnimatePinDots { dot1, dot2, dot3, dot4 ->
                animateDotsUseCase.invoke(
                    dot1, dot2, dot3, dot4, needReturn = false, truePIN = true
                ) {
                    viewModelScope.launchIO {
                        _effects.emit(AuthActivityEffect.NavigateToMainScreen)
                        _effects.emit(AuthActivityEffect.Finish)
                    }
                }
            })
        } else {
            _effects.emit(AuthActivityEffect.AnimatePinDots { dot1, dot2, dot3, dot4 ->
                animateDotsUseCase.invoke(
                    dot1, dot2, dot3, dot4, needReturn = true, truePIN = false, onEnd = {
                        viewModelScope.launchIO {
                            _state.update { it.copy(pin = "", dots = allWhiteDots()) }
                            unblockKeyBoard()
                        }
                    })
            })
        }
    }
}
