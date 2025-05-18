package ru.surf.learn2invest.presentation.ui.components.screens.fragments.subhistory

import ru.surf.learn2invest.domain.domain_models.Transaction

/**
 * Состояние экрана истории транзакций.
 *
 * @property data Список транзакций для отображения.
 */
internal data class SubHistoryFragmentState(
    val data: List<Transaction> = listOf()
)
