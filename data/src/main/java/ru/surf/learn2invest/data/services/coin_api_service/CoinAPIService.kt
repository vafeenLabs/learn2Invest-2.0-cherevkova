package ru.surf.learn2invest.data.services.coin_api_service

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.surf.learn2invest.data.network_components.responses.APIWrapper
import ru.surf.learn2invest.data.network_components.responses.AugmentedCoinReviewResponse
import ru.surf.learn2invest.data.network_components.responses.CoinPriceResponse
import ru.surf.learn2invest.data.network_components.responses.CoinReviewResponse

/**
 * Сервис сетевого взаимодействия
 */
internal interface CoinAPIService {

    // Получение данных о всех активах на рынке
    @GET(RetrofitLinks.API_MARKET_REVIEW)
    suspend fun getMarketReview(): APIWrapper<List<CoinReviewResponse>>

    // Получение истории изменения курса конкретного актива
    @GET(RetrofitLinks.API_HISTORY)
    suspend fun getCoinHistory(
        @Path("id") id: String,
        @Query("interval") interval: String,
        @Query("start") start: Long,
        @Query("end") end: Long
    ): APIWrapper<List<CoinPriceResponse>>

    // Получение подробных данных о конкретном активе
    @GET(RetrofitLinks.API_COIN_REVIEW)
    suspend fun getCoinReview(
        @Path("id") id: String
    ): APIWrapper<AugmentedCoinReviewResponse>
}