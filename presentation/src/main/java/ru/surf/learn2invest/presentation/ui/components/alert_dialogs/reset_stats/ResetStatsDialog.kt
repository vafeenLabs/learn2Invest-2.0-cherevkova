package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.reset_stats

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.SimpleDialogBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.parent.CustomAlertDialog

/**
 * Диалог для сброса статистики.
 *
 * Этот диалог предоставляет пользователю возможность сбросить статистику (баланс) в приложении.
 * Включает в себя кнопки для подтверждения сброса или отмены действия.
 *
 * @constructor Инициализирует диалог с использованием ViewModel для сброса статистики.
 */
@AndroidEntryPoint
internal class ResetStatsDialog : CustomAlertDialog() {

    /**
     * Тег диалога, используемый для идентификации.
     */
    override val dialogTag: String = "ResetStatsDialog"

    /**
     * Инициализирует слушателей для кнопок в диалоге.
     *
     * Устанавливает текст для кнопки подтверждения сброса и настраивает обработчики кликов для
     * кнопок "Да, точно" и "Нет". При подтверждении сброса статистики запускается соответствующий
     * процесс через ViewModel, а диалог закрывается.
     */
    override fun initListeners(binding: SimpleDialogBinding) {

        // Обработчик для кнопки "Да, точно" — сбрасывает статистику и закрывает диалог
        binding.yesExactly.setOnClickListener {
            viewModel.handleIntent(ResetStatsDialogIntent.ResetStats)
        }

        // Обработчик для кнопки "Нет" — просто закрывает диалог
        binding.no.setOnClickListener {
            viewModel.handleIntent(ResetStatsDialogIntent.Dismiss)
        }
        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                // Устанавливаем текст для кнопки сброса статистики
                binding.text.text = state.text
            }
        }

        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ResetStatsDialogEffect.Dismiss -> {
                        dismiss()
                    }

                    ResetStatsDialogEffect.ToastResetStateSuccessful -> {
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.stat_reset),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    /**
     * ViewModel для работы с логикой сброса статистики.
     */
    private val viewModel by viewModels<ResetStatsDialogViewModel>()
}
