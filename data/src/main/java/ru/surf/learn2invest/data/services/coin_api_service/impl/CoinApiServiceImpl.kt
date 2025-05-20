package ru.surf.learn2invest.data.services.coin_api_service.impl

import android.util.Log
import com.example.backenddto.api.BackendDtoAPI
import kotlinx.serialization.json.Json
import ru.surf.learn2invest.data.network_components.responses.APIWrapper
import ru.surf.learn2invest.data.network_components.responses.AugmentedCoinReviewResponse
import ru.surf.learn2invest.data.network_components.responses.CoinPriceResponse
import ru.surf.learn2invest.data.network_components.responses.CoinReviewResponse
import ru.surf.learn2invest.data.services.coin_api_service.CoinAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CoinApiServiceImpl @Inject constructor() : CoinAPIService {
    override suspend fun getMarketReview(): APIWrapper<List<CoinReviewResponse>> =
        BackendDtoAPI.assets(20)
            .fromJsonString()

    override suspend fun getCoinHistory(
        id: String,
        interval: String,
        start: Long,
        end: Long
    ): APIWrapper<List<CoinPriceResponse>> =
        BackendDtoAPI.assetsIdHistory(id, start, end).fromJsonString()

    override suspend fun getCoinReview(id: String): APIWrapper<AugmentedCoinReviewResponse> =
        BackendDtoAPI.assetsId(id).fromJsonString()

    private inline fun <reified T> String.fromJsonString() =
        Json.decodeFromString<T>(this.also { Log.e("body", it) }).also {
            Log.e("T", it.toString())
        }
}