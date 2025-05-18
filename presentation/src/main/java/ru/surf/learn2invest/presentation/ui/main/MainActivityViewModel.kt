package ru.surf.learn2invest.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.surf.learn2invest.domain.animator.usecase.AnimateAlphaUseCase
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.domain.utils.withContextMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivity
import ru.surf.learn2invest.presentation.ui.components.screens.sign_up.SignUpActivity
import javax.inject.Inject

/**
 * ViewModel для экрана главной активности. Управляет данными профиля пользователя и анимациями.
 *
 * @param settingsManager Менеджер профиля, предоставляющий данные профиля и возможности для обновления профиля.
 * @param animateAlphaUseCase Используется для анимации изменения прозрачности (alpha) у вида.
 */
@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val animateAlphaUseCase: AnimateAlphaUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _effects = MutableSharedFlow<MainActivityEffect>()
    val effects = _effects.asSharedFlow()

    fun handleIntent(intent: MainActivityIntent) {
        viewModelScope.launchIO {
            when (intent) {
                is MainActivityIntent.ProcessSplash -> processSplash(intent.textView)
            }
        }
    }

    /**
     * Поток, содержащий данные профиля пользователя.
     * Используется для наблюдения за изменениями профиля в UI.
     */
    private val profileFlow = settingsManager.settingsFlow
    private suspend fun processSplash(textView: TextView) {
        val profile = profileFlow.value
        if (profile.firstName != "undefined" &&
            profile.lastName != "undefined" &&
            profile.hash != null
        ) {
            withContextMAIN {
                textView.alpha = 0f
                textView.text =
                    "${context.getString(R.string.hello)}, ${profileFlow.value.firstName}!"
                animateAlphaUseCase.invoke(
                    view = textView,
                    duration = 2000,
                    onEnd = {
                        viewModelScope.launchIO {
                            _effects.emit(MainActivityEffect.StartIntent {
                                AuthActivity.newInstanceSignIN(it)
                            })
                            _effects.emit(MainActivityEffect.Finish)
                        }
                    },
                    values = floatArrayOf(
                        0f,
                        1f
                    )
                )
            }
        } else {
            delay(2000)
            _effects.emit(MainActivityEffect.StartIntent {
                Intent(it, SignUpActivity::class.java)
            })
            _effects.emit(MainActivityEffect.Finish)
        }
    }

}
