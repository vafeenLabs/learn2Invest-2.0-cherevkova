package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.sign_up

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.animator.usecase.AnimateDotsUseCase
import ru.surf.learn2invest.domain.cryptography.FingerprintAuthenticator
import ru.surf.learn2invest.domain.cryptography.usecase.UpdatePinUseCase
import ru.surf.learn2invest.domain.cryptography.usecase.VerifyPINUseCase
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivityEffect
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivityState
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivityViewModel
import javax.inject.Inject

/**
 * ViewModel для экрана регистрации PIN-кода.
 *
 * Управляет процессом ввода и подтверждения нового PIN-кода,
 * а также интеграцией с биометрической аутентификацией.
 *
 * @property context Контекст приложения для доступа к ресурсам.
 * @property updatePinUseCase UseCase для обновления PIN-кода в профиле.
 * @property animateDotsUseCase UseCase для анимации точек PIN.
 * @property fingerprintAuthenticator Менеджер биометрической аутентификации.
 */
@HiltViewModel
internal class AuthSignUpActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updatePinUseCase: UpdatePinUseCase,
    animateDotsUseCase: AnimateDotsUseCase,
    private val fingerprintAuthenticator: FingerprintAuthenticator,
    verifyPINUseCase: VerifyPINUseCase,
    settingsManager: SettingsManager,
) : AuthActivityViewModel(
    initialState = AuthActivityState(
        mainText = context.getString(R.string.change_PIN),
        isFingerprintButtonShowed = false
    ),
    settingsManager = settingsManager,
    verifyPINUseCase = verifyPINUseCase,
    animateDotsUseCase = animateDotsUseCase
) {
    private var firstPin = ""

    /**
     * Обрабатывает событие полного ввода PIN-кода.
     *
     * Логика:
     * - При первом вводе сохраняет PIN и запрашивает повтор.
     * - При совпадении PIN обновляет профиль и запускает анимацию,
     *   затем предлагает настроить биометрию (если доступна).
     * - При несовпадении запускает анимацию ошибки и сбрасывает ввод.
     */
    override suspend fun onPinIsFull() {
        val state = _state.value
        when {
            firstPin.isEmpty() -> {
                firstPin = state.pin
                delay(500)
                _state.update {
                    it.copy(
                        dots = allWhiteDots(),
                        mainText = context.getString(R.string.repeat_pin),
                        pin = ""
                    )
                }
                unblockKeyBoard()
            }

            state.pin == firstPin -> {
                updatePinUseCase.invoke(state.pin)
                _effects.emit(AuthActivityEffect.AnimatePinDots { dot1, dot2, dot3, dot4 ->
                    animateDotsUseCase.invoke(
                        dot1,
                        dot2,
                        dot3,
                        dot4,
                        needReturn = false,
                        truePIN = true,
                        onEnd = {
                            viewModelScope.launchIO {
                                if (fingerprintAuthenticator.isBiometricAvailable()) {
                                    _effects.emit(
                                        AuthActivityEffect.FingerPrintBottomSheet(
                                            onSuccess = {
                                                viewModelScope.launchIO {
                                                    settingsManager.update {
                                                        it.copy(biometry = true)
                                                    }
                                                    _effects.emit(AuthActivityEffect.NavigateToMainScreen)
                                                    _effects.emit(AuthActivityEffect.Finish)
                                                }
                                            },
                                            onCancel = {
                                                viewModelScope.launchIO {
                                                    settingsManager.update {
                                                        it.copy(biometry = false)
                                                    }
                                                    _effects.emit(AuthActivityEffect.NavigateToMainScreen)
                                                    _effects.emit(AuthActivityEffect.Finish)
                                                }
                                            })
                                    )
                                }
                            }
                        })
                })
            }

            state.pin != firstPin -> {
                _effects.emit(AuthActivityEffect.AnimatePinDots { dot1, dot2, dot3, dot4 ->
                    animateDotsUseCase.invoke(
                        dot1,
                        dot2,
                        dot3,
                        dot4,
                        needReturn = true,
                        truePIN = false,
                        onEnd = {
                            viewModelScope.launchIO {
                                _state.update { it.copy(pin = "", dots = allWhiteDots()) }
                                unblockKeyBoard()
                            }
                        })
                })
            }
        }
    }
}
