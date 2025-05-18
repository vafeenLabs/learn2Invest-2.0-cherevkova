package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.delete_profile

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.databinding.SimpleDialogBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.parent.CustomAlertDialog
import ru.surf.learn2invest.presentation.ui.main.MainActivity

/**
 * Диалоговое окно для удаления профиля пользователя.
 * Запрашивает подтверждение перед удалением и очищает базу данных.
 */
@AndroidEntryPoint
internal class DeleteProfileDialog : CustomAlertDialog() {

    /** Тег диалога для логирования и управления. */
    override val dialogTag: String = "DeleteProfileDialog"

    /** ViewModel для выполнения удаления профиля. */
    private val viewModel by viewModels<DeleteProfileDialogViewModel>()

    /**
     * Инициализирует обработчики нажатий для кнопок подтверждения и отмены.
     */
    override fun initListeners(binding: SimpleDialogBinding) {
        binding.yesExactly.setOnClickListener {
            viewModel.handle(DeleteProfileDialogIntent.DeleteProfile)
        }

        binding.no.setOnClickListener {
            viewModel.handle(DeleteProfileDialogIntent.Dismiss)
        }

        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                binding.text.text = state.text
            }
        }
        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.effect.collect { effect ->
                when (effect) {
                    DeleteProfileDialogEffect.Dismiss -> {
                        dismiss()
                    }

                    DeleteProfileDialogEffect.DismissAndRestartApp -> {
                        requireActivity().finish()
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        dismiss()
                    }
                }
            }
        }
    }
}