package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.services.ProfileManager
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
    private val verifyTradingPasswordUseCase: VerifyTradingPasswordUseCase,
    private val profileManager: ProfileManager,
) {

    /**
     * Проверяет, является ли введённый пароль торговли верным или не определённым.
     * @param password    Пароль торговли для проверки.
     * @return True, если пароль верный или не определён (хэш отсутствует), иначе false.
     */
    operator fun invoke(password: String): Boolean =
        if (profileManager.profileFlow.value.tradingPasswordHash != null) {
            verifyTradingPasswordUseCase.invoke(tradingPassword = password)
        } else true
}