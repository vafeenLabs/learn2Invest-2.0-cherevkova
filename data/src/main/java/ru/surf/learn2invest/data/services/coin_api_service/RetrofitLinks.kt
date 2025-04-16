package ru.surf.learn2invest.data.services.coin_api_service

/**
 * Константы для репозитория сетевого взаимодействия
 */
object RetrofitLinks {
    const val BASE_URL = "http://10.0.2.2:8080/"
    const val API_MARKET_REVIEW = "assets?limit=1000"
    const val API_HISTORY = "assets/{id}/history"
    const val API_COIN_REVIEW = "assets/{id}"
    const val WEEK: Long = 604800000 // миллисекунды
    const val INTERVAL: String =
        "d1" // интервал выборки данных. Доступные форматы m1, m5, m15, m30, h1, h2, h6, h12, d1
}