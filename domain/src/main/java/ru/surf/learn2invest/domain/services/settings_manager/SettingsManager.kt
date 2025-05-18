package ru.surf.learn2invest.domain.services.settings_manager

import kotlinx.coroutines.flow.StateFlow
import ru.surf.learn2invest.domain.domain_models.Settings

interface SettingsManager {
    val settingsFlow: StateFlow<Settings>
    fun update(updating: (Settings) -> Settings)
}