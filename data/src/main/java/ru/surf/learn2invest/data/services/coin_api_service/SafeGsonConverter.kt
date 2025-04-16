package ru.surf.learn2invest.data.services.coin_api_service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken


class SafeGsonConverter(val gson: Gson = Gson()) {

    // Для одиночных объектов
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            gson.fromJson(json, object : TypeToken<T>() {}.type)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    // Для списков
    inline fun <reified T> fromJsonList(json: String): List<T>? {
        return try {
            gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    // Сериализация объектов
    inline fun <reified T> toJson(obj: T): String? {
        return try {
            gson.toJson(obj, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            null
        }
    }

    // Сериализация списков
    inline fun <reified T> toJsonList(list: List<T>): String? {
        return try {
            gson.toJson(list, object : TypeToken<List<T>>() {}.type)
        } catch (e: Exception) {
            null
        }
    }
}