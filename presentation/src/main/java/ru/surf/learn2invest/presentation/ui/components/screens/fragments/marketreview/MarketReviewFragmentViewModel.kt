package ru.surf.learn2invest.presentation.ui.components.screens.fragments.marketreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.database.usecase.ClearSearchedCoinUseCase
import ru.surf.learn2invest.domain.database.usecase.GetAllSearchedCoinUseCase
import ru.surf.learn2invest.domain.database.usecase.InsertSearchedCoinUseCase
import ru.surf.learn2invest.domain.domain_models.CoinReview
import ru.surf.learn2invest.domain.network.ResponseResult
import ru.surf.learn2invest.domain.network.usecase.GetCoinReviewUseCase
import ru.surf.learn2invest.domain.network.usecase.GetMarketReviewUseCase
import ru.surf.learn2invest.domain.toCoinReview
import ru.surf.learn2invest.domain.utils.launchIO
import javax.inject.Inject

/**
 * ViewModel для экрана MarketReview, который управляет состоянием данных для отображения
 * информации о монетах и их фильтрации.
 *
 * @property getMarkerReviewUseCase Используется для получения списка всех рыночных обзоров.
 * @property insertSearchedCoinUseCase Используется для добавления монет в список поисковых запросов.
 * @property getAllSearchedCoinUseCase Используется для получения всех ранее добавленных монет в поисковых запросах.
 * @property getCoinReviewUseCase Используется для получения подробной информации о конкретной монете.
 * @property clearSearchedCoinUseCase Используется для очистки списка поисковых запросов.
 */
@HiltViewModel
internal class MarketReviewFragmentViewModel @Inject constructor(
    private val getMarkerReviewUseCase: GetMarketReviewUseCase,
    private val insertSearchedCoinUseCase: InsertSearchedCoinUseCase,
    private val getAllSearchedCoinUseCase: GetAllSearchedCoinUseCase,
    private val getCoinReviewUseCase: GetCoinReviewUseCase,
    private val clearSearchedCoinUseCase: ClearSearchedCoinUseCase,
) : ViewModel() {


    /**
     * Флаг, указывающий, был ли уже применен фильтр по цене.
     */
    private var firstTimePriceFilter = true

    /**
     * Индекс первого элемента, который должен быть обновлен при загрузке новых данных.
     */
    private var firstUpdateElement = 0

    /**
     * Количество элементов, которые должны быть обновлены.
     */
    private var amountUpdateElement = 0

    private val _state = MutableStateFlow(MarketReviewFragmentState())
    val state = _state.asStateFlow()
    fun handleIntent(intent: MarketReviewFragmentIntent) {
        viewModelScope.launchIO {
            when (intent) {
                MarketReviewFragmentIntent.ClearSearchData -> {
                    clearSearchData()
                }

                MarketReviewFragmentIntent.FilterByMarketCap -> {
                    filterByMarketcap()
                }

                MarketReviewFragmentIntent.FilterByPercent -> {
                    filterByPercent()
                }

                MarketReviewFragmentIntent.FilterByPrice -> {
                    filterByPrice()
                }

                is MarketReviewFragmentIntent.SetSearchState -> {
                    _state.update {
                        val copy = it.copy(isSearch = intent.isSearch)
                        if (!intent.isSearch) {
                            copy.copy(searchRequest = "")
                        } else copy
                    }
                }

                is MarketReviewFragmentIntent.UpdateData -> {
                    updateData(intent.firstElement, intent.lastElement)
                }

                is MarketReviewFragmentIntent.UpdateSearchRequest -> {
                    updateSearchRequest(intent.searchRequest)
                }

                is MarketReviewFragmentIntent.AddSearchedCoin -> {
                    if (_state.value.isSearch) {
                        insertSearchedCoinUseCase.invoke(intent.searchedCoin)
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launchIO {
            getAllSearchedCoinUseCase.invoke().collect { data ->
                val searchedIds = data.map { it.coinID }
                _state.update { state ->
                    state.copy(searchedData = state.data.filter { it.id in searchedIds })
                }
            }
        }
    }

    /**
     * Инициализирует данные, выполняет загрузку рыночных обзоров.
     */
    private suspend fun initData() {
        when (val result = getMarkerReviewUseCase()) {
            is ResponseResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        data = result.value.toMutableList().filter {
                            it.marketCapUsd > 0f && it.priceUsd > 0.1f
                        }.sortedByDescending { it.marketCapUsd }
                    )
                }
            }

            is ResponseResult.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                    )
                }
            }
        }
    }

    /**
     * Инициализация данных, выполняется при создании ViewModel.
     * Получает рыночные обзоры и обновляет состояние данных.
     */
    init {
        viewModelScope.launchIO {
            initData()
        }
    }

    /**
     * Активирует состояние фильтра для выбранного элемента.
     *
     * @param filterState Идентификатор фильтра.
     */
    private fun activateFilterState(filterState: FilterState) {
        if (filterState != FilterState.FILTER_BY_PRICE) firstTimePriceFilter = true
        _state.update {
            it.copy(filterState = filterState)
        }
    }

    /**
     * Сортирует данные по рыночной капитализации.
     */
    private fun filterByMarketcap() {
        activateFilterState(FilterState.FILTER_BY_MARKETCAP)
        _state.update {
            it.copy(data = it.data.sortedByDescending { element -> element.marketCapUsd })
        }
    }

    /**
     * Сортирует данные по процентному изменению за 24 часа.
     */
    private fun filterByPercent() {
        activateFilterState(FilterState.FILTER_BY_PERCENT)
        _state.update {
            it.copy(data = it.data.sortedByDescending { element -> element.changePercent24Hr })
        }
    }

    /**
     * Сортирует данные по цене.
     */
    private fun filterByPrice() {
        activateFilterState(FilterState.FILTER_BY_PRICE)
        if (!firstTimePriceFilter) {
            _state.update {
                it.copy(filterOrder = !it.filterOrder)
            }
        } else firstTimePriceFilter = false
        _state.update {
            it.copy(data = if (it.filterOrder) it.data.sortedBy { element -> element.priceUsd }
                .toMutableList()
            else it.data.sortedByDescending { element -> element.priceUsd }.toMutableList())
        }
    }

    /**
     * Устанавливает состояние поиска и обновляет данные с учетом поискового запроса.
     * @param searchRequest Строка поискового запроса.
     */
    private fun updateSearchRequest(searchRequest: String = "") {
        _state.update {
            it.copy(
                searchRequest = searchRequest,
                dataBySearchRequest = if (searchRequest.isNotEmpty()) {
                    it.data.filter { element ->
                        searchRequest in element.name ||
                                searchRequest in element.symbol ||
                                searchRequest in element.id
                    }
                } else {
                    listOf()
                })

        }
    }

    /**
     * Обновляет данные для отображения в указанном диапазоне элементов.
     *
     * @param firstElement Индекс первого элемента.
     * @param lastElement Индекс последнего элемента.
     */
    private suspend fun updateData(firstElement: Int, lastElement: Int) {
        val tempUpdate = mutableListOf<CoinReview>()
        val state = state.value
        val updateDestinationLink = if (state.isSearch) state.searchedData else state.data
        if (updateDestinationLink.isNotEmpty()
            && firstElement != NO_POSITION
            && updateDestinationLink.size > lastElement
        ) {
            firstUpdateElement = firstElement
            amountUpdateElement = lastElement - firstElement + 1
            for (index in firstElement..lastElement) {
                when (val result =
                    getCoinReviewUseCase.invoke(updateDestinationLink[index].id)) {
                    is ResponseResult.Success -> {
                        tempUpdate.add(result.value.toCoinReview())
                    }

                    is ResponseResult.Error -> _state.update {
                        it.copy(isError = true)
                    }
                }
            }
            val tempUpdateId = tempUpdate.map { it.id }

            _state.update {
                if (state.isSearch) {
                    it.copy(searchedData = it.searchedData.mapNotNull { element ->
                        if (tempUpdateId.contains(element.id)) tempUpdate.find { updateElement ->
                            updateElement.id == element.id
                        }
                        else element
                    })
                } else {
                    it.copy(data = it.data.mapNotNull { element ->
                        if (tempUpdateId.contains(element.id)) tempUpdate.find { updateElement ->
                            updateElement.id == element.id
                        }
                        else element
                    })
                }
            }
        } else initData()
    }

    /**
     * Очищает данные поиска.
     */
    private suspend fun clearSearchData() {
        clearSearchedCoinUseCase()
    }
}
