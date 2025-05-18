package ru.surf.learn2invest.domain.cryptography

import androidx.appcompat.app.AppCompatActivity

/**
 * Интерфейс для аутентификации пользователя с помощью отпечатка пальца.
 */
interface FingerprintAuthenticator {
    /**
     * Проверяет, доступно ли биометрическое аппаратное обеспечение на устройстве.
     * @return True, если биометрическое обеспечение доступно, false иначе.
     */
    fun isBiometricAvailable(): Boolean

    /**
     * Устанавливает callback-функцию, вызываемую при успешной аутентификации.
     *
     * @param function Callback-функция, которая будет вызвана при успешной аутентификации.
     * @return Экземпляр FingerprintAuthenticator для цепного вызова методов.
     */
    fun setSuccessCallback(function: () -> Unit): FingerprintAuthenticator

    /**
     * Устанавливает callback-функцию, вызываемую при неуспешной аутентификации.
     *
     * @param function Callback-функция, которая будет вызвана при неуспешной аутентификации.
     * @return Экземпляр FingerprintAuthenticator для цепного вызова методов.
     */
    fun setFailedCallback(function: () -> Unit): FingerprintAuthenticator

    /**
     * Устанавливает callback-функцию, вызываемую при отмене аутентификации пользователем.
     *
     * @param function Callback-функция, которая будет вызвана при отмене аутентификации.
     * @return Экземпляр FingerprintAuthenticator для цепного вызова методов.
     */
    fun setCancelCallback(function: () -> Unit): FingerprintAuthenticator

    /**
     * Настройка дизайна BottomSheet для аутентификации.
     *
     * @param title Название BottomSheet.
     * @param cancelText Текст кнопки отмены.
     * @return Экземпляр FingerprintAuthenticator для цепного вызова методов.
     */
    fun setDesignBottomSheet(title: String, cancelText: String): FingerprintAuthenticator

    /**
     * Запускает процесс аутентификации с помощью отпечатка пальца.
     * @param activity Активити, в которой выполняется аутентификация.
     */
    suspend fun auth(activity: AppCompatActivity)
}
