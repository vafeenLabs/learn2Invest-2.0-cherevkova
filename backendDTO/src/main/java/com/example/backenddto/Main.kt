package com.example.backenddto

import com.example.backenddto.dto.CoinPriceResponse
import com.example.backenddto.dto.CoinReviewResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Глобальный кэш текущих цен
private val coinPrices = mutableMapOf(
    "bitcoin" to 84375.27f,
    "ethereum" to 4521.64f,
    "xrp" to 5.95f
)

/**
 * Генерирует список исторических данных по цене криптовалюты с защитой от отрицательных значений.
 *
 * @param coinId Идентификатор криптовалюты (например, "bitcoin").
 * @param start Время начала периода в миллисекундах с эпохи.
 * @param end Время окончания периода в миллисекундах с эпохи.
 * @return Список объектов [CoinPriceResponse], содержащих цену и временную метку за каждый час.
 */
internal fun generateDailyHistory(coinId: String, start: Long, end: Long): List<CoinPriceResponse> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeStep = (end - start) / 24
    val lowerCoinId = coinId.lowercase()

    // Получаем текущую цену или устанавливаем дефолтную
    var currentPrice = coinPrices[lowerCoinId] ?: run {
        val newPrice = 100f + Random.nextFloat() * 100
        coinPrices[lowerCoinId] = newPrice
        newPrice
    }

    return (0..23).map { i ->
        val time = start + i * timeStep

        // Генерируем процент изменения (-5% до +5%)
        val changePercent = (Random.nextFloat() * 0.1f) - 0.05f
        currentPrice *= (1 + changePercent)

        // Гарантируем минимальную цену (0.01 USD)
        currentPrice = maxOf(0.01f, currentPrice)

        // Обновляем кэш цен
        coinPrices[lowerCoinId] = currentPrice

        CoinPriceResponse(
            priceUsd = currentPrice,
            time = time,
            date = dateFormat.format(Date(time))
        )
    }
}

/**
 * Генерирует список тестовых криптовалют с динамическими значениями,
 * включая популярные монеты и случайные.
 *
 * Для популярных монет (bitcoin, ethereum, xrp) значения цены и других параметров
 * изменяются с небольшой случайной вариацией при каждом вызове.
 *
 * @param count Общее количество криптовалют для генерации.
 * @return Список объектов [CoinReviewResponse] с актуальными данными.
 */
internal fun generateCryptoCoins(count: Int): List<CoinReviewResponse> {
    val popularCoinsIds = listOf("bitcoin", "ethereum", "xrp")

    val popularCoins = popularCoinsIds.mapIndexed { index, id ->
        // Генерируем случайную цену в диапазоне ±5% от текущей или базовой цены
        val basePrice = coinPrices[id] ?: when (id) {
            "bitcoin" -> 50000f
            "ethereum" -> 4000f
            "xrp" -> 1f
            else -> 100f
        }
        val newPrice = basePrice * (0.95f + Random.nextFloat() * 0.1f)
        coinPrices[id] = newPrice

        // Генерируем остальные поля с некоторой вариацией
        when (id) {
            "bitcoin" -> CoinReviewResponse(
                id = id,
                rank = 1,
                symbol = "BTC",
                name = "Bitcoin",
                maxSupply = 21000000f,
                volumeUsd24Hr = 2.5E10f * (0.9f + Random.nextFloat() * 0.2f),
                supply = 1.89E7f * (0.9f + Random.nextFloat() * 0.2f),
                marketCapUsd = newPrice * 1.89E7f,
                priceUsd = newPrice,
                changePercent24Hr = (Random.nextFloat() * 2) - 1,
                vwap24Hr = newPrice * (0.95f + Random.nextFloat() * 0.1f)
            )

            "ethereum" -> CoinReviewResponse(
                id = id,
                rank = 2,
                symbol = "ETH",
                name = "Ethereum",
                supply = 1.2E8f * (0.9f + Random.nextFloat() * 0.2f),
                maxSupply = 0f,
                marketCapUsd = newPrice * 1.2E8f,
                volumeUsd24Hr = 1.5E10f * (0.9f + Random.nextFloat() * 0.2f),
                priceUsd = newPrice,
                changePercent24Hr = (Random.nextFloat() * 2) - 1,
                vwap24Hr = newPrice * (0.95f + Random.nextFloat() * 0.1f)
            )

            "xrp" -> CoinReviewResponse(
                id = id,
                rank = 3,
                symbol = "XRP",
                name = "XRP",
                supply = 4.5E10f * (0.9f + Random.nextFloat() * 0.2f),
                maxSupply = 1.0E11f,
                marketCapUsd = newPrice * 4.5E10f,
                volumeUsd24Hr = 1.2E9f * (0.9f + Random.nextFloat() * 0.2f),
                priceUsd = newPrice,
                changePercent24Hr = (Random.nextFloat() * 2) - 1,
                vwap24Hr = newPrice * (0.95f + Random.nextFloat() * 0.1f)
            )

            else -> throw IllegalArgumentException("Unknown coin id")
        }
    }

    val randomCoins = (1..(count - popularCoins.size)).map { index ->
        val id = "crypto-$index"
        val basePrice = coinPrices[id] ?: maxOf(100f + Random.nextFloat() * 100, 0.01f).also {
            coinPrices[id] = it
        }

        // Генерируем случайные значения для всех полей
        val newPrice = basePrice * (0.95f + Random.nextFloat() * 0.1f)
        coinPrices[id] = newPrice

        CoinReviewResponse(
            id = id,
            rank = popularCoins.size + index,
            symbol = "CRYPTO${index.toString().padStart(3, '0')}",
            name = "CryptoCoin $index",
            supply = Random.nextFloat() * 1_000_000_000f,
            maxSupply = Random.nextFloat() * 2_000_000_000f,
            marketCapUsd = newPrice * (Random.nextFloat() * 1_000_000f),
            volumeUsd24Hr = Random.nextFloat() * 10_000_000_000f,
            priceUsd = newPrice,
            changePercent24Hr = (Random.nextFloat() * 2) - 1,
            vwap24Hr = newPrice * (0.95f + Random.nextFloat() * 0.1f)
        )
    }

    return popularCoins + randomCoins
}

/**
 * Находит криптовалюту по идентификатору среди сгенерированных тестовых данных.
 *
 * @param id Идентификатор криптовалюты для поиска.
 * @return Объект [CoinReviewResponse] с данными криптовалюты.
 * @throws NoSuchElementException если криптовалюта с указанным id не найдена.
 */
internal fun findCryptoCoin(id: String): CoinReviewResponse =
    generateCryptoCoins(2000).first { it.id == id }
