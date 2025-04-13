package ru.surf.learn2invest.presentation.ui.components.screens.fragments.marketreview

import ru.surf.learn2invest.domain.domain_models.CoinReview

data class MarketReviewFragmentState(
    val data: List<CoinReview> = listOf(),
    val searchedData: List<CoinReview> = listOf(),
    val filterOrder: Boolean = true,
    val filterState: FilterState = FilterState.FILTER_BY_MARKETCAP,
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val isSearch: Boolean = false,
)