package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.domain_models.Profile
import javax.inject.Inject

/**
 * Юз-кейс для проверки, является введённый пароль торговли верным или не определённым.
 *
 * Этот класс проверяет, если хэш пароля торговли существует в профиле пользователя,
 * то он проверяет пароль с помощью другого юз-кейса. Если хэш отсутствует, считается, что пароль не определён и возвращается true.
 */
class IsTrueTradingPasswordOrIsNotDefinedUseCase @Inject constructor(
    /**
     * Юз-кейс для проверки пароля торговли.
     */
    private val verifyTradingPasswordUseCase: VerifyTradingPasswordUseCase
) {

    /**
     * Проверяет, является ли введённый пароль торговли верным или не определённым.
     *
     * @param profile     Профиль пользователя, содержащий хэш пароля торговли.
     * @param password    Пароль торговли для проверки.
     * @return True, если пароль верный или не определён (хэш отсутствует), иначе false.
     */
    operator fun invoke(profile: Profile, password: String): Boolean =
        if (profile.tradingPasswordHash != null) {
            verifyTradingPasswordUseCase.invoke(user = profile, tradingPassword = password)
        } else true
}