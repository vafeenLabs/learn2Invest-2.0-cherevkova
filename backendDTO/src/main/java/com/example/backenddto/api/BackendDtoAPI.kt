package com.example.backenddto.api

import com.example.backenddto.dto.APIWrapper
import com.example.backenddto.findCryptoCoin
import com.example.backenddto.generateCryptoCoins
import com.example.backenddto.generateDailyHistory
import kotlinx.serialization.json.Json

object BackendDtoAPI {
    private inline fun <reified T> T.toJsonString() = Json.encodeToString(this)
    fun assets(limit: Int) =
        APIWrapper(
            data = generateCryptoCoins(limit.coerceAtMost(2000)), info = APIWrapper.Info(
                coins_num = limit,
                System.currentTimeMillis()
            )
        )
            .let {
                Json.encodeToString(it)
            }

    fun assetsId(id: String) = APIWrapper(
        data = findCryptoCoin(id), info = APIWrapper.Info(
            coins_num = 1,
            System.currentTimeMillis()
        )
    ).toJsonString()

    fun assetsIdHistory(coinId: String, start: Long, end: Long) =
        APIWrapper(
            data = generateDailyHistory(
                coinId = coinId,
                start = start,
                end = end
            ), info = APIWrapper.Info(
                coins_num = 1,
                System.currentTimeMillis()
            )
        ).toJsonString()
}
