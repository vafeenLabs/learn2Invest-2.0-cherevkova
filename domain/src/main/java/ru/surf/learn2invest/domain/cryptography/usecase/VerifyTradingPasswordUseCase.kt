package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.services.ProfileManager
import javax.inject.Inject

/**
 * Юз-кейс для проверки торгового пароля.
 *
 * Проверяет соответствие введённого торгового пароля хэшированному значению,
 * хранящемуся в профиле пользователя.
 */
class VerifyTradingPasswordUseCase @Inject constructor(
    /**
     * Юз-кейс для получения хэшированного значения пароля.
     * Используется для генерации хэша из введённого пароля.
     */
    private val getHashedPasswordUseCase: GetHashedPasswordUseCase,
    private val profileManager: ProfileManager,
) {

    /**
     * Сравнивает хэш введённого пароля с сохранённым значением.

     * @param tradingPassword   Введённый пароль для проверки
     * @return true - если хэш введённого пароля совпадает с сохранённым значением,
     *         false - в случае несоответствия
     */
    operator fun invoke(tradingPassword: String): Boolean {
        val user = profileManager.profileFlow.value
        return getHashedPasswordUseCase.invoke(user, tradingPassword) == user.tradingPasswordHash
    }
}
