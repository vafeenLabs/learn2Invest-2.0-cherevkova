package com.example.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.random.Random

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CORS) { anyHost() }

        routing {
            // Эндпоинт: /assets?limit=2000
            get("/assets") {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 2000
                val coins = generateCryptoCoins(limit.coerceAtMost(2000))
                call.respond(CryptoListResponse(data = coins))
            }

            // Эндпоинт: /assets/{id}
            get("/assets/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val coin = findCryptoCoin(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(CryptoDetailResponse(data = coin))
            }
            get("/assets/{id}/history") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val interval = call.request.queryParameters["interval"] ?: "d1"
                val start = call.request.queryParameters["start"]?.toLongOrNull()
                    ?: (System.currentTimeMillis() - 86400000 * 7) // 7 дней назад по умолчанию
                val end = call.request.queryParameters["end"]?.toLongOrNull()
                    ?: System.currentTimeMillis()

                if (interval != "d1") {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Only daily interval (d1) is supported")
                    )
                    return@get
                }

                val history = generateDailyHistory(id, start, end)
                call.respond(CryptoPriceListResponse(data = history))
            }
        }
    }.start(wait = true)
}

// Генератор исторических данных (24 точки за последние 24 часа)
internal fun generateDailyHistory(coinId: String, start: Long, end: Long): List<CoinPriceResponse> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeStep = (end - start) / 24 // Равные интервалы

    return (0..23).map { i ->
        val time = start + i * timeStep
        val basePrice = when (coinId.lowercase(getDefault())) {
            "bitcoin" -> 80000f + Random.nextFloat() * 10000
            "ethereum" -> 4000f + Random.nextFloat() * 1000
            "ripple" -> 0.9f + Random.nextFloat() * 0.2f
            else -> 100f + Random.nextFloat() * 100
        }

        CoinPriceResponse(
            priceUsd = basePrice + (Random.nextFloat() * 200 - 100), // +/- 100 от базовой цены
            time = time,
            date = dateFormat.format(Date(time))
        )
    }
}

// Генерация тестовых криптовалют
internal fun generateCryptoCoins(count: Int): List<CoinReviewResponse> {
    val popularCoins = listOf(
        CoinReviewResponse(
            id = "bitcoin",
            rank = 1,
            symbol = "BTC",
            name = "Bitcoin",
            supply = 18938281f,
            maxSupply = 21000000f,
            marketCapUsd = 1674213186750.20f,
            volumeUsd24Hr = 25159311166f,
            priceUsd = 84375.27f,
            changePercent24Hr = 0.75f,
            vwap24Hr = 84200.50f
        ),
        CoinReviewResponse(
            id = "ethereum",
            rank = 2,
            symbol = "ETH",
            name = "Ethereum",
            supply = 120000000f,
            maxSupply = 0f, // Для Ethereum maxSupply не установлен
            marketCapUsd = 542184567890.12f,
            volumeUsd24Hr = 15432123456f,
            priceUsd = 4521.64f,
            changePercent24Hr = 1.25f,
            vwap24Hr = 4500.00f
        ),
        CoinReviewResponse(
            id = "ripple",
            rank = 3,
            symbol = "XRP",
            name = "XRP",
            supply = 45000000000f,
            maxSupply = 100000000000f,
            marketCapUsd = 45123456789.01f,
            volumeUsd24Hr = 1234567890f,
            priceUsd = 0.95f,
            changePercent24Hr = -0.35f,
            vwap24Hr = 0.94f
        )
    )

    val randomCoins = (1..(count - popularCoins.size)).map {
        CoinReviewResponse(
            id = "crypto-$it",
            rank = popularCoins.size + it,
            symbol = "CRYPTO${it.toString().padStart(3, '0')}",
            name = "CryptoCoin $it",
            supply = Random.nextFloat() * 1000000000,
            maxSupply = Random.nextFloat() * 2000000000,
            marketCapUsd = Random.nextFloat() * 1000000000000,
            volumeUsd24Hr = Random.nextFloat() * 10000000000,
            priceUsd = Random.nextFloat() * 10000,
            changePercent24Hr = (Random.nextFloat() * 10) - 5, // От -5% до +5%
            vwap24Hr = Random.nextFloat() * 10000
        )
    }

    return popularCoins + randomCoins
}

internal fun findCryptoCoin(id: String): CoinReviewResponse? {
    return generateCryptoCoins(2000).firstOrNull { it.id == id }
}

@Serializable
internal data class CoinPriceResponse(
    val priceUsd: Float,
    val time: Long,
    val date: String,
)

// Модели данных
@Serializable
internal data class CryptoPriceListResponse(
    val data: List<CoinPriceResponse>,
    val info: Info = Info(coins_num = data.size, time = System.currentTimeMillis() / 1000)
)
// Модели данных
@Serializable
internal data class CryptoListResponse(
    val data: List<CoinReviewResponse>,
    val info: Info = Info(coins_num = data.size, time = System.currentTimeMillis() / 1000)
)

@Serializable
internal data class CryptoDetailResponse(
    val data: CoinReviewResponse,
    val info: Info = Info(coins_num = 1, time = System.currentTimeMillis() / 1000)
)

@Serializable
internal data class Info(
    val coins_num: Int,
    val time: Long
)

@Serializable
internal data class CoinReviewResponse(
    val id: String,
    val rank: Int,
    val symbol: String,
    val name: String,
    val supply: Float,
    val maxSupply: Float,
    val marketCapUsd: Float,
    val volumeUsd24Hr: Float,
    val priceUsd: Float,
    val changePercent24Hr: Float,
    val vwap24Hr: Float,
)