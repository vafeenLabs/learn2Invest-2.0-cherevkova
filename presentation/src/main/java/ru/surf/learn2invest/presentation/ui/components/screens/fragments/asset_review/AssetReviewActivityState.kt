package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_review

/**
 * Состояние экрана обзора актива.
 *
 * @property isOverviewSelected Флаг, указывающий, выбран ли обзор (true) или другая вкладка.
 * @property id Идентификатор актива.
 * @property name Название актива.
 * @property symbol Символ актива.
 */
internal data class AssetReviewActivityState(
    val isOverviewSelected: Boolean = true,
    val id: String,
    val name: String,
    val symbol: String,
)
