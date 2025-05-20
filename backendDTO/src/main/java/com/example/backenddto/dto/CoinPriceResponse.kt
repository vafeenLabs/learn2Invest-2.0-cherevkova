package com.example.backenddto.dto

import kotlinx.serialization.Serializable

/**
 * Ответ от API, содержащий историю изменения цены криптовалюты.
 *
 * Используется для парсинга данных о динамике цен.
 *
 * @property priceUsd Цена коина в долларах США.
 * @property time Временная метка в миллисекундах с начала эпохи (Unix timestamp).
 * @property date Человеко-читаемая дата в формате (например, "YYYY-MM-DD HH:mm:ss").
 */
@Serializable
internal data class CoinPriceResponse(
    val priceUsd: Float,
    val time: Long,
    val date: String,
)
