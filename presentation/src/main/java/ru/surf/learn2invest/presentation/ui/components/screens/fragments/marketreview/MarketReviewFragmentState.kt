package ru.surf.learn2invest.presentation.ui.components.screens.fragments.marketreview

import ru.surf.learn2invest.domain.domain_models.CoinReview

/**
 * Состояние экрана обзора рынка.
 *
 * @property data Основной список активов монет.
 * @property searchedData Список активов, соответствующих результатам поиска.
 * @property dataBySearchRequest Список активов, отфильтрованных по поисковому запросу.
 * @property filterOrder Порядок сортировки (true - по возрастанию, false - по убыванию).
 * @property filterState Текущее состояние фильтра (например, сортировка по капитализации).
 * @property isError Флаг наличия ошибки загрузки данных.
 * @property isLoading Флаг состояния загрузки данных.
 * @property isSearch Флаг активности режима поиска.
 * @property searchRequest Текущий поисковый запрос.
 */
internal data class MarketReviewFragmentState(
    val data: List<CoinReview> = listOf(),
    val searchedData: List<CoinReview> = listOf(),
    val dataBySearchRequest: List<CoinReview> = listOf(),
    val filterOrder: Boolean = true,
    val filterState: FilterState = FilterState.FILTER_BY_MARKETCAP,
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val isSearch: Boolean = false,
    val searchRequest: String = "",
) {
    override fun toString(): String {
        return "data=$data\n\nsearchedData=$searchedData\n\ndataBySearchRequest=$dataBySearchRequest\n\nfilterOrder=$filterOrder\n\n$filterState\n\n$isError\n\n$isLoading\n\nsearchRequest=$searchRequest"
    }
}
