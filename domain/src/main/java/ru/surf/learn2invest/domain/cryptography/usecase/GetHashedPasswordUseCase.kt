package ru.surf.learn2invest.domain.cryptography.usecase

import ru.surf.learn2invest.domain.cryptography.PasswordHasher
import ru.surf.learn2invest.domain.domain_models.Profile
import javax.inject.Inject

/**
 * Юз-кейс для получения хэшированного пароля.
 *
 * Этот класс отвечает за преобразование пароля в хэш с использованием дополнительной информации из профиля пользователя.
 */
class GetHashedPasswordUseCase @Inject constructor(
    /**
     * Хэшер паролей, используемый для преобразования пароля в хэш.
     */
    private val passwordHasher: PasswordHasher
) {

    /**
     * Вычисляет хэш пароля, используя имя и фамилию пользователя.
     *
     * @param user      Профиль пользователя, содержащий имя и фамилию.
     * @param password  Пароль, который необходимо хэшировать.
     * @return Хэшированный пароль.
     */
    operator fun invoke(user: Profile, password: String) = passwordHasher.passwordToHash(
        firstName = user.firstName,
        lastName = user.lastName,
        password = password
    )
}
