package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.common

import ru.surf.learn2invest.domain.domain_models.AssetInvest
import ru.surf.learn2invest.domain.domain_models.Profile

/**
 * Состояние диалога продажи актива.
 *
 * Этот класс представляет состояние диалога, в котором отображается информация о текущем активе,
 * количестве лотов для продажи и торговом пароле.
 *
 * @param coin Информация об активе, который продается.
 * @param lotsData Данные о количестве лотов для продажи.
 * @param tradingPassword Торговый пароль для выполнения продажи.
 */
internal data class BuySellDialogState(
    val coin: AssetInvest,
    val lotsData: LotsData = LotsData(0),
    val tradingPassword: String = "",
    val profile: Profile,
    val currentPrice: Float? = null,
)