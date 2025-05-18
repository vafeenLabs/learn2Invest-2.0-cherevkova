package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.cryptography.PasswordHasher
import ru.surf.learn2invest.domain.services.ProfileManager
import javax.inject.Inject

class UpdateTradingPasswordUseCase @Inject constructor(
    private val profileManager: ProfileManager,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke(newTradingPassword: String) {
        profileManager.updateProfile {
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