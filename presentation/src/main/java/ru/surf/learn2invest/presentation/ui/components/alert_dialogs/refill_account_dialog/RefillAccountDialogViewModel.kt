package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.refill_account_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.services.ProfileManager
import ru.surf.learn2invest.domain.utils.launchIO
import javax.inject.Inject

/**
 * ViewModel для управления логикой пополнения баланса.
 *
 * @property profileManager Менеджер профиля, используемый для обновления данных пользователя.
 */
@HiltViewModel
internal class RefillAccountDialogViewModel @Inject constructor(
    private val profileManager: ProfileManager,
) : ViewModel() {

    /** Поток данных профиля пользователя. */
    private val profileFlow = profileManager.profileFlow
    private val _state =
        MutableStateFlow(RefillAccountDialogState(balance = profileFlow.value.fiatBalance))
    val state = _state.asStateFlow()
    private val _effects = MutableSharedFlow<RefillAccountDialogEffect>()
    val effects = _effects.asSharedFlow()
    fun handeIntent(intent: RefillAccountDialogIntent) {
        viewModelScope.launchIO {
            when (intent) {
                RefillAccountDialogIntent.ClearBalance -> {
                    clearBalance()
                }

                RefillAccountDialogIntent.Refill -> {
                    refill()
                }

                RefillAccountDialogIntent.RemoveLastCharFromBalance -> {
                    removeLastCharFromBalance()
                }

                is RefillAccountDialogIntent.AddCharToBalance -> {
                    addCharToBalance(intent.char)
                }
            }
        }
    }

    /**
     * Добавляет символ к текущей строке введённого баланса.
     *
     * @param s Символ, который нужно добавить.
     */
    private fun addCharToBalance(s: String) {
        _state.update { it.copy(enteredBalance = "${it.enteredBalance}${s}") }
    }

    /**
     * Удаляет последний введённый символ из строки баланса.
     */
    private fun removeLastCharFromBalance() {
        _state.update { it.copy(enteredBalance = it.enteredBalance.dropLast(1)) }
    }

    /**
     * Очищает введённый баланс.
     */
    private fun clearBalance() {
        _state.update { it.copy(enteredBalance = "") }
    }

    /**
     * Пополняет баланс, если введённое значение является корректным числом больше нуля.
     *
     * @return `true`, если пополнение успешно, иначе `false`.
     */
    private suspend fun refill(): Boolean {
        val enteredBalance = _state.value.enteredBalance.toFloatOrNull()
        return if (enteredBalance != null && enteredBalance > 0) {
            profileManager.updateProfile {
                it.copy(fiatBalance = it.fiatBalance + enteredBalance)
            }
            _effects.emit(RefillAccountDialogEffect.Dismiss)
            true
        } else {
            false
        }
    }
}
