package ru.surf.learn2invest.presentation.ui.components.screens.fragments.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.database.usecase.GetAllTransactionUseCase
import ru.surf.learn2invest.domain.utils.launchIO
import javax.inject.Inject

/**
 * ViewModel для [HistoryFragment]. Получает данные всех транзакций и обрабатывает их.
 */
@HiltViewModel
internal class HistoryFragmentViewModel @Inject constructor(
    private val getAllTransactionUseCase: GetAllTransactionUseCase
) :
    ViewModel() {
    private val _state = MutableStateFlow(HistoryFragmentState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launchIO {
            getAllTransactionUseCase.invoke().map { it.reversed() }.collect { data ->
                _state.update {
                    it.copy(data = data)
                }
            }

        }
    }
}
