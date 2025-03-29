package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.domain_models.Profile
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
    private val getHashedPasswordUseCase: GetHashedPasswordUseCase
) {

    /**
     * Сравнивает хэш введённого пароля с сохранённым значением.
     *
     * @param user              Профиль пользователя с хранимым хэшем торгового пароля (tradingPasswordHash)
     * @param tradingPassword   Введённый пароль для проверки
     * @return true - если хэш введённого пароля совпадает с сохранённым значением,
     *         false - в случае несоответствия
     */
    operator fun invoke(user: Profile, tradingPassword: String): Boolean =
        getHashedPasswordUseCase.invoke(user, tradingPassword) == user.tradingPasswordHash
}
