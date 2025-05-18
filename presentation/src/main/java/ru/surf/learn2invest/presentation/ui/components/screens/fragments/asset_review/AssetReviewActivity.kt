package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_review

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.ActivityAssetReviewBinding
import ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview.AssetOverviewFragment
import ru.surf.learn2invest.presentation.ui.components.screens.fragments.subhistory.SubHistoryFragment
import ru.surf.learn2invest.presentation.utils.NoArgException
import ru.surf.learn2invest.presentation.utils.setNavigationBarColor
import ru.surf.learn2invest.presentation.utils.setStatusBarColor
import ru.surf.learn2invest.presentation.utils.viewModelCreator
import javax.inject.Inject

/**
 * Экран обзора актива, позволяющий пользователю просматривать подробности актива,
 * его историю и совершать действия с активом (покупка/продажа).
 */
@AndroidEntryPoint
internal class AssetReviewActivity : AppCompatActivity() {
    @Inject
    lateinit var factory: AssetReviewActivityViewModel.Factory

    private val viewModel: AssetReviewActivityViewModel by viewModelCreator {
        factory.createAssetReviewActivityViewModel(
            intent.getStringExtra(ID_KEY) ?: throw NoArgException(ID_KEY),
            intent.getStringExtra(NAME_KEY) ?: throw NoArgException(NAME_KEY),
            intent.getStringExtra(SYMBOL_KEY) ?: throw NoArgException(SYMBOL_KEY)
        )
    }

    /**
     * Инициализация экрана и привязка компонентов UI.
     * Устанавливаются цвета для статус бара и навигационной панели, инициализируются кнопки
     * и фрагменты для отображения.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Установка цветов для статус бара и навигационной панели
        setStatusBarColor(window, this, R.color.white, R.color.main_background_dark)
        setNavigationBarColor(window, this, R.color.white, R.color.main_background_dark)

        // Инициализация привязки и привязка UI
        val binding = ActivityAssetReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.handleIntent(AssetReviewActivityIntent.LoadIcon(binding.coinIcon))
        initListeners(binding)
    }


    private fun initListeners(binding: ActivityAssetReviewBinding) {
        lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                binding.coinName.text = state.name
                binding.coinSymbol.text = state.symbol
                // Определение текущей темы (темная или светлая)
                val isDarkTheme =
                    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

                val accentColor =
                    getColor(if (isDarkTheme) R.color.accent_background_dark else R.color.accent_background)
                val defaultColor =
                    getColor(if (isDarkTheme) R.color.accent_button_dark else R.color.view_background)
                // Установка цвета фона для кнопок "Обзор" и "История"
                binding.assetOverviewBtn.backgroundTintList = ColorStateList.valueOf(
                    if (state.isOverviewSelected) accentColor else defaultColor
                )

                binding.assetHistoryBtn.backgroundTintList = ColorStateList.valueOf(
                    if (!state.isOverviewSelected) accentColor else defaultColor
                )
                if (state.isOverviewSelected) {
                    goToFragment(
                        AssetOverviewFragment.newInstance(
                            state.id,
                            state.name,
                            state.symbol
                        )
                    )
                } else {
                    goToFragment(SubHistoryFragment.newInstance(state.symbol))
                }
            }
        }
        lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    AssetReviewActivityEffect.GoBack -> {
                        finish()
                    }
                }
            }
        }

        // Обработчик клика на кнопку "Обзор"
        binding.assetOverviewBtn.setOnClickListener {
            viewModel.handleIntent(AssetReviewActivityIntent.OpenAssetOverViewFragment)
        }

        // Обработчик клика на кнопку "История"
        binding.assetHistoryBtn.setOnClickListener {
            viewModel.handleIntent(AssetReviewActivityIntent.OpenSubHistoryFragment)
        }
        // Обработчик кнопки возврата
        binding.goBack.setOnClickListener {
            viewModel.handleIntent(AssetReviewActivityIntent.GoBack)
        }
    }

    /**
     * Отмена загрузки иконки при уничтожении активности.
     */
    override fun onDestroy() {
        viewModel.handleIntent(AssetReviewActivityIntent.CancelLoadingImage)
        super.onDestroy()
    }

    /**
     * Переход на новый фрагмент.
     */
    private fun goToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val NAME_KEY = "NAME_KEY"
        private const val SYMBOL_KEY = "SYMBOL_KEY"
        fun newIntent(activity: AppCompatActivity, id: String, name: String, symbol: String) =
            Intent(activity, AssetReviewActivity::class.java).apply {
                putExtra(ID_KEY, id)
                putExtra(NAME_KEY, name)
                putExtra(SYMBOL_KEY, symbol)
            }
    }
}