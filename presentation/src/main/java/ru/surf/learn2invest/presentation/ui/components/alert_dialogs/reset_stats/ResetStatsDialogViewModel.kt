package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.reset_stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.surf.learn2invest.domain.database.usecase.ClearAppDatabaseUseCase
import ru.surf.learn2invest.domain.services.ProfileManager
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.domain.utils.withContextIO
import ru.surf.learn2invest.presentation.R
import javax.inject.Inject

/**
 * ViewModel для диалога сброса статистики.
 *
 * Этот ViewModel управляет логикой сброса статистики пользователя, включая обнуление баланса
 * и очистку базы данных приложения.
 *
 * @param profileManager Менеджер профиля, используемый для управления профилем пользователя.
 * @param clearAppDatabaseUseCase Используется для очистки базы данных приложения.
 */
@HiltViewModel
internal class ResetStatsDialogViewModel @Inject constructor(
    private val profileManager: ProfileManager,
    private val clearAppDatabaseUseCase: ClearAppDatabaseUseCase,
    @ApplicationContext val context: Context
) : ViewModel() {
    private val _effects = MutableSharedFlow<ResetStatsDialogEffect>()
    val effects = _effects.asSharedFlow()
    private val _state = MutableStateFlow(
        ResetStatsDialogState(
            text = context.getString(R.string.reset_stats)
        )
    )
    val state = _state.asStateFlow()

    fun handleIntent(intent: ResetStatsDialogIntent) {
        viewModelScope.launchIO {
            when (intent) {
                ResetStatsDialogIntent.ResetStats -> {
                    resetStats()
                }

                ResetStatsDialogIntent.Dismiss -> {
                    _effects.emit(ResetStatsDialogEffect.Dismiss)
                }
            }
        }
    }

    /**
     * Сбрасывает статистику пользователя, обнуляя баланс и очищая базу данных.
     *
     * Эта функция:
     * 1. Обновляет профиль пользователя, сбрасывая баланс средств.
     * 2. Очищает базу данных приложения.
     * 3. Показывает уведомление пользователю о том, что статистика была сброшена.
     *
     * @param context Контекст приложения, необходимый для отображения уведомления.
     */
    private suspend fun resetStats() {
        // Создаем копию текущего профиля с обнулением балансов
        val savedProfile = profileManager.profileFlow.value.copy(
            fiatBalance = 0f,
            assetBalance = 0f
        )

        // Обработка сброса данных в фоновом потоке
        withContextIO {
            clearAppDatabaseUseCase() // Очищаем базу данных
            profileManager.updateProfile {
                savedProfile // Обновляем профиль
            }
        }

        // Показ уведомления о сбросе статистики
        _effects.emit(ResetStatsDialogEffect.ToastResetStateSuccessful)
        _effects.emit(ResetStatsDialogEffect.Dismiss)
    }
}