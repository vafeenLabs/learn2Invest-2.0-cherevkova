package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview

internal sealed class AssetOverviewFragmentEffect {
    data class OpenBuyDialog(val id: String, val name: String, val symbol: String) :
        AssetOverviewFragmentEffect()

    data class OpenSellDialog(val id: String, val name: String, val symbol: String) :
        AssetOverviewFragmentEffect()
}