package ru.surf.learn2invest.presentation.ui.components.screens.fragments.marketreview

import ru.surf.learn2invest.domain.domain_models.SearchedCoin

internal sealed class MarketReviewFragmentIntent {
    data object FilterByMarketCap : MarketReviewFragmentIntent()
    data object FilterByPercent : MarketReviewFragmentIntent()
    data object FilterByPrice : MarketReviewFragmentIntent()
    data class SetSearchState(val isSearch: Boolean) :
        MarketReviewFragmentIntent()

    data class UpdateSearchRequest(val searchRequest: String) : MarketReviewFragmentIntent()

    data object ClearSearchData : MarketReviewFragmentIntent()
    data class UpdateData(val firstElement: Int, val lastElement: Int) :
        MarketReviewFragmentIntent()

    data class AddSearchedCoin(val searchedCoin: SearchedCoin) : MarketReviewFragmentIntent()
}