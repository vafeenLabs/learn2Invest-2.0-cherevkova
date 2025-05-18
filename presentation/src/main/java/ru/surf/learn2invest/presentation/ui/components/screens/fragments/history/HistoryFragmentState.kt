package ru.surf.learn2invest.presentation.ui.components.screens.fragments.history

import ru.surf.learn2invest.domain.domain_models.Transaction

/**
 * Состояние экрана истории транзакций.
 *
 * @property data Список транзакций для отображения.
 */
internal data class HistoryFragmentState(
    val data: List<Transaction> = listOf()
)