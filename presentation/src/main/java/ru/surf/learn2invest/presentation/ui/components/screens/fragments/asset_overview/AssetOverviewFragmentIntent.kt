package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview

import com.github.mikephil.charting.charts.LineChart

internal sealed class AssetOverviewFragmentIntent {
    data object StopUpdatingPriceFLow : AssetOverviewFragmentIntent()
    data object StartUpdatingPriceFLow : AssetOverviewFragmentIntent()
    data class SetupChartAndLoadChartData(val lineChart: LineChart) : AssetOverviewFragmentIntent()
    data object BuyAsset : AssetOverviewFragmentIntent()
    data object SellAsset : AssetOverviewFragmentIntent()
}