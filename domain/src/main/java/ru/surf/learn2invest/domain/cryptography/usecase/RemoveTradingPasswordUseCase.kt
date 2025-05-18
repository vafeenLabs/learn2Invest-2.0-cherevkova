package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import javax.inject.Inject

class RemoveTradingPasswordUseCase @Inject constructor(
    private val settingsManager: SettingsManager,
) {
    suspend operator fun invoke() {
        settingsManager.update { it.copy(tradingPasswordHash = null) }
    }
}