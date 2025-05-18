package ru.surf.learn2invest.presentation.ui.components.screens.fragments.portfolio

import com.github.mikephil.charting.data.Entry
import ru.surf.learn2invest.domain.domain_models.AssetInvest
import java.util.Date

/**
 * Состояние экрана портфолио.
 *
 * @property chartData Данные для отображения графика (список точек Entry).
 * @property fiatBalance Баланс в фиатной валюте.
 * @property assetBalance Баланс в активах.
 * @property assets Список инвестируемых активов.
 * @property priceChanges Карта изменений цен активов (ключ - идентификатор актива, значение - изменение).
 * @property portfolioChangePercentage Процент изменения всего портфеля.
 * @property dates Список дат, соответствующих точкам графика.
 */
internal data class PortfolioFragmentState(
    val chartData: List<Entry> = listOf(),
    val fiatBalance: Float = 0f,
    val assetBalance: Float = 0f,
    val assets: List<AssetInvest> = listOf(),
    val priceChanges: Map<String, Float> = mapOf(),
    val portfolioChangePercentage: Float = 0f,
    val dates: List<Date> = listOf()
) {
    /**
     * Общий баланс портфеля - сумма фиатного и активного балансов.
     */
    val totalBalance: Float
        get() = fiatBalance + assetBalance
}
