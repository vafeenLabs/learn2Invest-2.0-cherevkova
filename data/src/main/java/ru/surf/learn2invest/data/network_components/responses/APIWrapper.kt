package ru.surf.learn2invest.data.network_components.responses

/**
 * Обертка верхнего уровня для парсинга JSON
 */
internal data class APIWrapper<T>(
    val data: T,
    val info: Info = Info(
        coins_num = 0,
        time = System.currentTimeMillis() / 1000
    )
) {
    data class Info(
        val coins_num: Int,
        val time: Long
    )
}


