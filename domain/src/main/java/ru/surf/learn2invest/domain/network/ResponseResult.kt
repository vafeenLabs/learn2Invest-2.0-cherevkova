package ru.surf.learn2invest.domain.network

/**
 * Обертка для описания состояния полученных данных
 */
sealed class ResponseResult<T> {
    data class Success<T>(val value: T) : ResponseResult<T>()
    data class Error<T>(val e: Exception) : ResponseResult<T>()
}