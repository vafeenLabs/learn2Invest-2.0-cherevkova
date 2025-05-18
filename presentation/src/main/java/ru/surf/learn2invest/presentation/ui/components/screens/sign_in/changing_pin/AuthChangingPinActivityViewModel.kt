package ru.surf.learn2invest.presentation.ui.components.screens.sign_in.changing_pin

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.animator.usecase.AnimateDotsUseCase
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
 * ViewModel для экрана смены PIN-кода.
 *
 * Управляет процессом ввода старого PIN, нового PIN и подтверждения нового PIN.
 * Обрабатывает валидацию, анимацию точек и обновление PIN в профиле.
 *
 * @property context Контекст приложения для доступа к ресурсам.
 * @property animateDotsUseCase UseCase для анимации точек PIN.
 * @property updatePinUseCase UseCase для обновления PIN-кода.
 */
@HiltViewModel
internal class AuthChangingPinActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val animateDotsUseCase: AnimateDotsUseCase,
    private val updatePinUseCase: UpdatePinUseCase,
    verifyPINUseCase: VerifyPINUseCase,
    settingsManager: SettingsManager,
) : AuthActivityViewModel(
    initialState = AuthActivityState(mainText = context.getString(R.string.enter_old_pin)),
    settingsManager = settingsManager,
    verifyPINUseCase = verifyPINUseCase
) {
    private var isVerified = false
    private var firstPin = ""

    /**
     * Обрабатывает событие полного ввода PIN-кода.
     *
     * Логика:
     * - Если вводится старый PIN, проверяет его.
     * - Если старый PIN верен, переходит к вводу нового PIN.
     * - Если вводится новый PIN, просит повторить.
     * - При подтверждении нового PIN обновляет профиль или сбрасывает ввод при ошибке.
     */
    override suspend fun onPinIsFull() {
        val state = _state.value
        when {
            // ввод старого PIN
            firstPin.isEmpty() && !isVerified -> {
                val truth = verifyPIN()
                isVerified = truth
                _effects.emit(AuthActivityEffect.AnimatePinDots { dot1, dot2, dot3, dot4 ->
                    animateDotsUseCase.invoke(
                        dot1,
                        dot2,
                        dot3,
                        dot4,
                        needReturn = true,
                        truePIN = truth,
                        onEnd = {
                            viewModelScope.launchIO {
                                if (truth) {
                                    _state.update {
                                        it.copy(mainText = context.getString(R.string.enter_new_pin))
                                    }
                                }
                                _state.update {
                                    it.copy(
                                        pin = "",
                                        dots = allWhiteDots()
                                    )
                                }
                                unblockKeyBoard()
                            }
                        })
                })
            }

            // ввод нового PIN
            firstPin.isEmpty() && isVerified -> {
                firstPin = state.pin
                delay(500)
                _state.update {
                    it.copy(
                        pin = "",
                        mainText = context.getString(R.string.repeat_pin),
                        dots = allWhiteDots()
                    )
                }
                unblockKeyBoard()
            }

            // повторение нового PIN
            firstPin.isNotEmpty() && isVerified -> {
                val truth = _state.value.pin == firstPin
                if (truth) {
                    updatePinUseCase.invoke(state.pin)
                }
                _effects.emit(AuthActivityEffect.AnimatePinDots { dot1, dot2, dot3, dot4 ->
                    animateDotsUseCase.invoke(
                        dot1,
                        dot2,
                        dot3,
                        dot4,
                        needReturn = !truth,
                        truePIN = truth,
                        onEnd = {
                            viewModelScope.launchIO {
                                if (truth) {
                                    _effects.emit(AuthActivityEffect.Finish)
                                } else {
                                    _state.update { it.copy(pin = "", dots = allWhiteDots()) }
                                    unblockKeyBoard()
                                }
                            }
                        })
                })

            }
        }
    }
}
