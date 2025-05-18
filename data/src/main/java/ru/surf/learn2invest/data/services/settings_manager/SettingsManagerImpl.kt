package ru.surf.learn2invest.data.services.settings_manager

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import ru.surf.learn2invest.data.converters.toSettings
import ru.surf.learn2invest.data.converters.toSettingsSerializable
import ru.surf.learn2invest.domain.domain_models.Settings
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import javax.inject.Inject

/**
 * Реализация [SettingsManager], управляющая настройками профиля пользователя
 * с использованием [SharedPreferences] для хранения данных.
 *
 * Класс предоставляет реактивный поток [settingsFlow], который позволяет наблюдать
 * за изменениями профиля в реальном времени.
 *
 * @property sharedPreferences Экземпляр [SharedPreferences] для сохранения и загрузки настроек.
 */
internal class SettingsManagerImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : SettingsManager {

    /**
     * Внутренний [MutableStateFlow], хранящий текущее состояние профиля.
     * Инициализируется значением из SharedPreferences или создаёт профиль по умолчанию.
     */
    private val _settingsFlow =
        MutableStateFlow(sharedPreferences.getSettingsOrCreateIfNull().toSettings())

    /**
     * Публичный [StateFlow], предоставляющий доступ к текущему состоянию профиля.
     * Позволяет подписчикам получать обновления при изменении настроек.
     */
    override val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()

    /**
     * Обновляет профиль, применяя функцию преобразования к текущему состоянию.
     * Новое состояние сохраняется в SharedPreferences и публикуется в [settingsFlow].
     *
     * Метод синхронизирован для предотвращения гонок при одновременном обновлении.
     *
     * @param updating Функция, принимающая текущий профиль и возвращающая обновлённый.
     */
    @Synchronized
    override fun update(updating: (Settings) -> Settings) {
        val newSettings = updating(_settingsFlow.value)
        Log.d("sp", "save ${newSettings.toSettingsSerializable().toJsonString()}")
        sharedPreferences.save(newSettings.toSettingsSerializable())
        _settingsFlow.value = newSettings
    }

    /**
     * Сохраняет объект [SettingsSerializable] в SharedPreferences в виде JSON-строки.
     *
     * @receiver Экземпляр [SharedPreferences].
     * @param settingsSerializable Объект настроек для сохранения.
     */
    private fun SharedPreferences.save(settingsSerializable: SettingsSerializable) =
        edit().apply {
            putString(
                SharedPreferencesValue.SETTINGS_KEY,
                settingsSerializable.toJsonString()
            )
            apply()
        }

    /**
     * Получает настройки из SharedPreferences.
     * Если настройки отсутствуют, создаёт новый объект по умолчанию, сохраняет его и возвращает.
     *
     * @receiver Экземпляр [SharedPreferences].
     * @return Объект [SettingsSerializable], полученный из хранилища или созданный по умолчанию.
     */
    private fun SharedPreferences.getSettingsOrCreateIfNull(): SettingsSerializable =
        getString(SharedPreferencesValue.SETTINGS_KEY, null)?.decodeSettings()
            ?: run {
                val newSettings = SettingsSerializable()
                save(newSettings)
                newSettings
            }

    /**
     * Сериализует объект [SettingsSerializable] в JSON-строку.
     *
     * @receiver Объект настроек.
     * @return JSON-представление объекта.
     */
    private fun SettingsSerializable.toJsonString(): String = Json.encodeToString(this)

    /**
     * Десериализует JSON-строку в объект [SettingsSerializable].
     *
     * @receiver JSON-строка с настройками.
     * @return Объект настроек.
     */
    private fun String.decodeSettings(): SettingsSerializable = Json.decodeFromString(this)
}
