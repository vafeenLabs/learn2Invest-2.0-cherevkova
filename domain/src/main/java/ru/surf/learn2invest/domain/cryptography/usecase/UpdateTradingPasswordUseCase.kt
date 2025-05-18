package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.cryptography.PasswordHasher
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import javax.inject.Inject

class UpdateTradingPasswordUseCase @Inject constructor(
    private val settingsManager: SettingsManager,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke(newTradingPassword: String) {
        settingsManager.update {
            it.copy(
                tradingPasswordHash = passwordHasher.passwordToHash(
                    firstName = it.firstName,
                    lastName = it.lastName,
                    password = newTradingPassword
                )
            )
        }
    }
}