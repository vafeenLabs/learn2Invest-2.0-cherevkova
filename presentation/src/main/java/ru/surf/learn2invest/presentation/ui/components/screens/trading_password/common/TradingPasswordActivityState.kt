package ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common

/**
 * Состояние экрана работы с торговым паролем.
 *
 * @property mainText Основной заголовок экрана.
 * @property mainButtonText Текст на основной кнопке действия
 * @property minLenTradingPasswordTV Состояние правила минимальной длины пароля:
 *                                   `true` - выполнено, `false` - нарушено, `null` - неактивно.
 * @property notMoreThan2TV Состояние правила "Не более 2 повторяющихся символов".
 * @property noSeqMoreThan3TV Состояние правила "Нет последовательностей длиннее 3 символов".
 * @property passMatchTV Состояние правила "Пароли совпадают".
 * @property oldPasCorrectTV Состояние правила "Старый пароль корректен".
 * @property passwordLastEditText Текст для подсказки/лейбла поля старого пароля (`null` если поле скрыто).
 * @property passwordEditEditText Текст для подсказки/лейбла поля нового пароля.
 * @property passwordConfirmEditText Текст для подсказки/лейбла поля подтверждения пароля.
 */
internal data class TradingPasswordActivityState(
    val mainText: String,
    val mainButtonText: String,
    val minLenTradingPasswordTV: Boolean? = null,
    val notMoreThan2TV: Boolean? = null,
    val noSeqMoreThan3TV: Boolean? = null,
    val passMatchTV: Boolean? = null,
    val oldPasCorrectTV: Boolean? = null,
    val passwordLastEditText: String? = null,
    val passwordEditEditText: String? = null,
    val passwordConfirmEditText: String? = null,
) {
    /**
     * Флаг доступности кнопки подтверждения.
     *
     * Возвращает `true` только если:
     * - Все активные правила выполнены (`false` или `null` считаются как нарушение)
     */
    val confirmButtonIsEnabled: Boolean
        get() = minLenTradingPasswordTV != false &&
                notMoreThan2TV != false &&
                noSeqMoreThan3TV != false &&
                passMatchTV != false &&
                oldPasCorrectTV != false
}
