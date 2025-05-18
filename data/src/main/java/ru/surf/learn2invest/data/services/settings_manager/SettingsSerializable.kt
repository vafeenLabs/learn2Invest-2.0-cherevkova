package ru.surf.learn2invest.data.services.settings_manager

import kotlinx.serialization.Serializable

/**
 * Профиль пользователя со всеми настройками
 * @param firstName [Имя ]
 * @param lastName [Фамилия ]
 * @param biometry  [Вкл/Выкл входа по биометрии]
 * @param fiatBalance  [Баланс обычных денег]
 * @param assetBalance  [Суммарная стоимость активов ]
 * @param hash   [Хэш PIN-кода]
 * @param tradingPasswordHash   [Хэш Торгового пароля]
 */
@Serializable
internal data class SettingsSerializable(
    val firstName: String = "",
    val lastName: String = "",
    val biometry: Boolean = false,
    val fiatBalance: Float = 0f,
    val assetBalance: Float = 0f,
    val hash: String? = null,
    val tradingPasswordHash: String? = null
)