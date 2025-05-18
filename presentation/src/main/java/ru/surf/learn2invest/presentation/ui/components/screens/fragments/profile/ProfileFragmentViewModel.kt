package ru.surf.learn2invest.presentation.ui.components.screens.fragments.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.cryptography.FingerprintAuthenticator
import ru.surf.learn2invest.domain.database.usecase.ClearAppDatabaseUseCase
import ru.surf.learn2invest.domain.domain_models.Settings
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.utils.launchIO
import javax.inject.Inject

/**
 * ViewModel для управления состоянием профиля пользователя в приложении.
 * Отвечает за обновление профиля, смену пароля для торговли, работу с биометрической аутентификацией,
 * сброс статистики и другие операции, связанные с профилем пользователя.
 *
 * @param settingsManager Менеджер профиля пользователя, который предоставляет доступ к данным
 *                       профиля и позволяет обновлять их.
 * @param clearAppDatabaseUseCase UseCase для очистки базы данных приложения.
 * @param fingerprintAuthenticator Аутентификатор для работы с биометрией пользователя.
 */
@HiltViewModel
internal class ProfileFragmentViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val clearAppDatabaseUseCase: ClearAppDatabaseUseCase,
    val fingerprintAuthenticator: FingerprintAuthenticator,
) : ViewModel() {

    // Поток данных профиля
    private val profileFlow = settingsManager.settingsFlow

    // состояние экрана
    private val _state = MutableStateFlow(
        ProfileFragmentState(
            firstName = "",
            lastName = "",
            tradingPasswordHash = "",
            biometry = false,
        )
    )
    val state = _state.asStateFlow()
    private val _effects = MutableSharedFlow<ProfileFragmentEffect>()
    val effects = _effects.asSharedFlow()
    fun handleIntent(intent: ProfileFragmentIntent) {
        viewModelScope.launchIO {
            when (intent) {
                is ProfileFragmentIntent.IsBiometricAvailable -> isBiometricAvailable()
                is ProfileFragmentIntent.BiometryBtnSwitch -> biometryBtnSwitch()
                ProfileFragmentIntent.ShowDeleteProfileDialogEffect -> showDeleteProfileDialog()
                ProfileFragmentIntent.ShowResetStatsDialogEffect -> showResetStatsDialog()
                ProfileFragmentIntent.ChangeTradingPassword -> changeTradingPassword()
                ProfileFragmentIntent.ChangeTransactionConfirmation -> changeTransactionConfirmation()
                ProfileFragmentIntent.ChangePIN -> changePIN()
            }
        }
    }

    init {
        viewModelScope.launchIO {
            profileFlow.collectLatest { profile ->
                _state.update {
                    it.copy(
                        firstName = profile.firstName,
                        lastName = profile.lastName,
                        tradingPasswordHash = profile.tradingPasswordHash,
                        biometry = profile.biometry,
                    )
                }
            }
        }
    }

    /**
     * Обновляет профиль пользователя с помощью переданной функции.
     *
     * @param updating Функция для обновления данных профиля.
     */
    suspend fun updateProfile(updating: (Settings) -> Settings) {
        settingsManager.update(updating)
    }


    /**
     * Изменяет настройку подтверждения транзакции, в зависимости от наличия пароля для торговли.
     */
    private suspend fun changeTransactionConfirmation() {
        _effects.emit(
            if (profileFlow.value.tradingPasswordHash == null)
                ProfileFragmentEffect.TradingPasswordActivityCreateTP
            else ProfileFragmentEffect.TradingPasswordActivityRemoveTP
        )
    }


    /**
     * Переключает настройку биометрической аутентификации для пользователя.
     */
    private suspend fun biometryBtnSwitch() {
        val profile = profileFlow.value
        if (profile.biometry) {
            viewModelScope.launchIO {
                updateProfile {
                    profile.copy(biometry = false)
                }
            }
        } else {
            _effects.emit(ProfileFragmentEffect.FingerPrintBottomSheet(onSuccess = {
                viewModelScope.launchIO {
                    updateProfile { profile.copy(biometry = true) }
                }
            }))
        }
    }

    /**
     * Запускает активность для изменения PIN-кода.
     */
    private suspend fun changePIN() = _effects.emit(ProfileFragmentEffect.SignInActivityChangingPIN)

    /**
     * Проверяет, доступна ли биометрическая аутентификация на устройстве.

     * @return true, если биометрическая аутентификация доступна, иначе false.
     */
    private fun isBiometricAvailable() {
        _state.update { it ->
            it.copy(isBiometryAvailable = fingerprintAuthenticator.isBiometricAvailable())
        }
    }

    private suspend fun showDeleteProfileDialog() =
        _effects.emit(ProfileFragmentEffect.ShowDeleteProfileDialogEffect)


    private suspend fun showResetStatsDialog() =
        _effects.emit(ProfileFragmentEffect.ShowResetStatsDialogEffect)


    private suspend fun changeTradingPassword() =
        _effects.emit(ProfileFragmentEffect.TradingPasswordActivityChangeTP)

}
