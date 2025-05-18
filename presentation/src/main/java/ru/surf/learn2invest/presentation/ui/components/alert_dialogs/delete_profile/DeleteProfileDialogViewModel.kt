package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.delete_profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.surf.learn2invest.domain.database.usecase.ClearAppDatabaseUseCase
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.presentation.R
import javax.inject.Inject

@HiltViewModel
internal class DeleteProfileDialogViewModel @Inject constructor(
    private val clearAppDatabaseUseCase: ClearAppDatabaseUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(
        DeleteProfileDialogState(
            text = context.getString(R.string.asking_to_delete_profile)
        )
    )
    val state = _state.asStateFlow()
    private val _effect = MutableSharedFlow<DeleteProfileDialogEffect>()
    val effect: SharedFlow<DeleteProfileDialogEffect> = _effect

    fun handle(intent: DeleteProfileDialogIntent) {
        when (intent) {
            DeleteProfileDialogIntent.DeleteProfile -> deleteProfile()
            DeleteProfileDialogIntent.Dismiss -> dismiss()
        }
    }

    private fun dismiss() {
        viewModelScope.launchIO {
            _effect.emit(DeleteProfileDialogEffect.Dismiss)
        }
    }

    private fun deleteProfile() {
        viewModelScope.launchIO {
            clearAppDatabaseUseCase()
            _effect.emit(DeleteProfileDialogEffect.DismissAndRestartApp)
        }
    }
}