package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.services.ProfileManager
import javax.inject.Inject

class RemoveTradingPasswordUseCase @Inject constructor(
    private val profileManager: ProfileManager,
) {
    suspend operator fun invoke() {
        profileManager.updateProfile { it.copy(tradingPasswordHash = null) }
    }
}