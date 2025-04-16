package ru.surf.learn2invest.data.network_components

import android.util.Log
import ru.surf.learn2invest.data.converters.AugmentedCoinReviewConverter
import ru.surf.learn2invest.data.converters.CoinPriceConverter
import ru.surf.learn2invest.data.converters.CoinReviewConverter
import ru.surf.learn2invest.domain.domain_models.AugmentedCoinReview
import ru.surf.learn2invest.domain.domain_models.CoinPrice
import ru.surf.learn2invest.domain.domain_models.CoinReview
import ru.surf.learn2invest.domain.network.NetworkRepository
import ru.surf.learn2invest.domain.network.ResponseResult
import ru.surf.learn2invest.data.services.coin_api_service.RetrofitLinks
import ru.surf.learn2invest.data.services.coin_api_service.CoinAPIService
import ru.surf.learn2invest.data.services.coin_api_service.SafeGsonConverter
import javax.inject.Inject

/**
 * Репозиторий для получения данных с API
 */

internal class NetworkRepositoryImpl @Inject constructor(
    private val coinAPIService: CoinAPIService,
    private val augmentedCoinReviewConverter: AugmentedCoinReviewConverter,
    private val coinPriceConverter: CoinPriceConverter,
    private val coinReviewConverter: CoinReviewConverter,
) : NetworkRepository {
    private val safeGsonConverter = SafeGsonConverter()


    override suspend fun getCoinReview(id: String): ResponseResult<AugmentedCoinReview> =
        try {
            val response = coinAPIService.getCoinReview(
                id = id.lowercase()
            )
            ResponseResult.Success(augmentedCoinReviewConverter.convert(response.data))
        } catch (e: Exception) {
            ResponseResult.Error(e)
        }

    override suspend fun getCoinHistory(id: String): ResponseResult<List<CoinPrice>> =
        try {
            val response = coinAPIService.getCoinHistory(
                id = id.lowercase(),
                interval = RetrofitLinks.INTERVAL,
                start = System.currentTimeMillis() - RetrofitLinks.WEEK,
                end = System.currentTimeMillis()
            )
            ResponseResult.Success(coinPriceConverter.convertList(response.data))
        } catch (e: Exception) {
            ResponseResult.Error(e)
        }

    override suspend fun getMarketReview(): ResponseResult<List<CoinReview>> =
        try {
            val response = coinAPIService.getMarketReview()
            ResponseResult.Success(coinReviewConverter.convertList(response.data))
        } catch (e: Exception) {
            ResponseResult.Error(e)
        }
}


