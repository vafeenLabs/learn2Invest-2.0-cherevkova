package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.cryptography.PasswordHasher
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import javax.inject.Inject

class UpdatePinUseCase @Inject constructor(
    private val settingsManager: SettingsManager,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke(newPin: String) {
        settingsManager.update {
            it.copy(
                hash = passwordHasher.passwordToHash(
                    firstName = it.firstName,
                    lastName = it.lastName,
                    password = newPin
                )
            )
        }
    }
}