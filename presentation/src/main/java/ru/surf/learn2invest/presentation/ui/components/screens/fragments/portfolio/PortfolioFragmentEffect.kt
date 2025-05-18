package ru.surf.learn2invest.presentation.ui.components.screens.fragments.portfolio

internal sealed class PortfolioFragmentEffect {
    data class StartAssetReviewActivity(
        val id: String,
        val name: String,
        val symbol: String
    ) : PortfolioFragmentEffect()

    data class OpenLink(val link: String) : PortfolioFragmentEffect()
    data class MailTo(val mail: String) : PortfolioFragmentEffect()
    data object OpenDrawer : PortfolioFragmentEffect()
    data object CloseDrawer : PortfolioFragmentEffect()
    data object OpenRefillAccountDialog : PortfolioFragmentEffect()
}