package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview

import ru.surf.learn2invest.domain.domain_models.AssetInvest
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.utils.formatAsPrice

/**
 * Класс, содержащий финансовые результаты актива, такие как изменения стоимости и процентный результат.
 */
internal data class FinResult(val resultCost: Float, val resultPercent: Float) {
    constructor(assetInvest: AssetInvest, actualPrice: Float) : this(
        ((actualPrice - assetInvest.coinPrice) * assetInvest.amount),
        ((actualPrice * assetInvest.amount) / (assetInvest.coinPrice * assetInvest.amount)).times(
            100
        ).minus(100),
    )

    /**
     * Метод для получения цвета в зависимости от результата (положительный, отрицательный или нейтральный).
     */
    fun color(): Int =
        if (resultCost < 0f) R.color.error else if (resultCost > 0f) R.color.increase else R.color.black

    /**
     * Форматированная строка для отображения результата.
     */
    fun formattedStr(): String =
        "${resultCost.formatAsPrice(2)} / ${resultPercent.formatAsPrice(2)}%"
}

/**
 * Состояние информации о монете. Может быть данными или пустым результатом.
 */
internal data class AssetOverviewState(
    val finResult: FinResult? = null, // Финансовый результат
    val coinCostResult: String? = null, // Общая стоимость активов с учетом текущей цены
    val coinPriceChangesResult: String? = null, // Изменения цены актива
    val price: Float? = null,
    val marketCap: Float? = null,
    val coin: AssetInvest? = null,
)