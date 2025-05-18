package ru.surf.learn2invest.presentation.ui.components.screens.fragments.subhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.database.usecase.GetFilteredBySymbolTransactionUseCase
import ru.surf.learn2invest.domain.utils.launchIO


/**
 * ViewModel для обработки данных по сделкам с конкретной монетой.
 * Отвечает за получение и хранение данных, связанных с транзакциями конкретной монеты.
 *
 * @param getFilteredBySymbolTransactionUseCase Используется для получения списка транзакций,
 *        отфильтрованных по символу монеты. Это бизнес-логика, которая предоставляет данные.
 * @param symbol Символ монеты, для которой нужно получить историю сделок. Этот параметр
 *        используется для фильтрации данных, получаемых через use case.
 */
internal class SubHistoryFragmentViewModel @AssistedInject constructor(
    /**
     * Используется для фильтрации и получения списка транзакций, которые относятся только к указанному символу монеты.
     * Это сервис или use case, который взаимодействует с репозиторием данных для получения информации.
     */
    getFilteredBySymbolTransactionUseCase: GetFilteredBySymbolTransactionUseCase,

    /**
     * Символ монеты, который передается в ViewModel. Это строка, которая представляет уникальный идентификатор монеты
     * (например, "BTC", "ETH" и т.д.), по которому будут отфильтрованы транзакции.
     */
    @Assisted val symbol: String,

    ) : ViewModel() {
    private val _state = MutableStateFlow(SubHistoryFragmentState())
    val state = _state.asStateFlow()
    fun handleIntent() {}

    init {
        viewModelScope.launchIO {
            getFilteredBySymbolTransactionUseCase(symbol).map { it.reversed() }.collect { data ->
                _state.update { it.copy(data = data) }
            }
        }
    }

    /**
     * Фабрика для создания ViewModel с передачей параметра символа монеты.
     * Эта фабрика используется для внедрения зависимости в ViewModel при ее создании.
     */
    @AssistedFactory
    interface Factory {
        /**
         * Функция для создания экземпляра [SubHistoryFragmentViewModel] с передачей символа монеты.
         *
         * @param symbol Символ монеты для фильтрации транзакций.
         */
        fun createSubHistoryAssetViewModel(symbol: String): SubHistoryFragmentViewModel
    }
}