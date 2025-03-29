package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.domain_models.Profile
import javax.inject.Inject

/**
 * Юз-кейс для проверки PIN-кода.
 *
 * Этот класс проверяет, соответствует ли введённый PIN-код хэшированному значению, хранящемуся в профиле пользователя.
 */
class VerifyPINUseCase @Inject constructor(
    /**
     * Юз-кейс для получения хэшированного пароля.
     */
    private val getHashedPasswordUseCase: GetHashedPasswordUseCase
) {

    /**
     * Проверяет, является ли введённый PIN-код верным.
     *
     * @param user       Профиль пользователя, содержащий хэшированное значение PIN-кода.
     * @param password   PIN-код для проверки.
     * @return True, если введённый PIN-код соответствует хэшированному значению, иначе false.
     */
    operator fun invoke(user: Profile, password: String): Boolean =
        getHashedPasswordUseCase.invoke(user, password) == user.hash
}


