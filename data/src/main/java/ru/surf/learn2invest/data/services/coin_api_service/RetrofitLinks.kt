package ru.surf.learn2invest.data.services.coin_api_service

/**
 * Константы для репозитория сетевого взаимодействия
 */
object RetrofitLinks {
    const val BASE_URL = "http://192.168.0.103:8080/"
    const val API_MARKET_REVIEW = "assets?limit=20"
    const val API_HISTORY = "assets/{id}/history"
    const val API_COIN_REVIEW = "assets/{id}"
    const val WEEK: Long = 604800000 // миллисекунды
    const val INTERVAL: String = "d1"
}