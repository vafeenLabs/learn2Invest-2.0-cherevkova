package ru.surf.learn2invest.data.network_components.responses

import kotlinx.serialization.Serializable

/**
 * Обертка верхнего уровня для парсинга JSON
 */
@Serializable
internal data class APIWrapper<T>(
    val data: T,
    val info: Info
) {
    @Serializable
    data class Info(
        val coins_num: Int,
        val time: Long
    )
}


