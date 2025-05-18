package ru.surf.learn2invest.presentation.ui.components.screens.fragments.portfolio

internal sealed class PortfolioFragmentIntent {
    data object StartRealtimeUpdate : PortfolioFragmentIntent()
    data object StopRealtimeUpdate : PortfolioFragmentIntent()
    data class StartAssetReviewActivity(
        val id: String,
        val name: String,
        val symbol: String
    ) : PortfolioFragmentIntent()

    data object OpenDrawer : PortfolioFragmentIntent()
    data object CloseDrawer : PortfolioFragmentIntent()
    data class OpenLink(val link: String) : PortfolioFragmentIntent()
    data class MailTo(val mail: String) : PortfolioFragmentIntent()
    data object OpenRefillAccountDialog : PortfolioFragmentIntent()
}