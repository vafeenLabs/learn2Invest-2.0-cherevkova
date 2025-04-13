package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.FragmentAssetOverviewBinding
import ru.surf.learn2invest.presentation.ui.components.chart.Last7DaysFormatter
import ru.surf.learn2invest.presentation.ui.components.chart.LineChartHelper
import ru.surf.learn2invest.presentation.ui.components.screens.fragments.common.BaseResFragment
import ru.surf.learn2invest.presentation.utils.NoArgException
import ru.surf.learn2invest.presentation.utils.formatAsPrice
import ru.surf.learn2invest.presentation.utils.getWithCurrency
import ru.surf.learn2invest.presentation.utils.viewModelCreator
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Фрагмент, отображающий обзор актива, включая график и финансовую информацию.
 * Использует [AssetOverViewFragmentViewModel] для получения данных и отображения их на экране.
 */
@AndroidEntryPoint
internal class AssetOverviewFragment : BaseResFragment() {
    @Inject
    lateinit var factory: AssetOverViewFragmentViewModel.Factory

    private val viewModel: AssetOverViewFragmentViewModel by viewModelCreator {
        factory.createAssetOverViewFragmentViewModel(
            id = requireArguments().getString(ID_KEY) ?: throw NoArgException(ID_KEY),
            symbol = requireArguments().getString(SYMBOL_KEY) ?: throw NoArgException(SYMBOL_KEY)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentAssetOverviewBinding.inflate(inflater, container, false)

        // Настройка и загрузка данных для графика
        viewModel.chartHelper = LineChartHelper(requireContext(), Last7DaysFormatter())
        viewModel.chartHelper.setupChart(binding.chart)
        viewModel.loadChartData()

        // Сбор и отображение информации о монете
        lifecycleScope.launchMAIN {
            viewModel.coinInfoFlow.collect { state ->
                Log.d("state", "$state")
                binding.coin.root.isVisible = state is CoinInfoState.Data
                if (state is CoinInfoState.Data) {
                    binding.coin.apply {
                        finResult.text = state.finResult.formattedStr()
                        finResult.setTextColor(getColorRes(state.finResult.color()))
                        coinsCost.text = state.coinCostResult
                        coinsPrice.text = state.coinPriceChangesResult
                        coinsCount.text = state.coinCount
                    }
                    binding.capitalisation.text = NumberFormat.getInstance(Locale.US).apply {
                        maximumFractionDigits = 0
                    }.format(state.marketCap).getWithCurrency()
                    binding.price.text = state.price.formatAsPrice(8).getWithCurrency()
                }
            }
        }

        // Определение темы (темная или светлая)
        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val accentColor =
            getColorRes(if (isDarkTheme) R.color.accent_background_dark else R.color.accent_background)

        // Установка фона для корневого элемента
        binding.coin.root.setCardBackgroundColor(accentColor)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.startRealTimeUpdate()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopRealTimeUpdateJob()
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val SYMBOL_KEY = "SYMBOL_KEY"

        /**
         * Создание нового экземпляра фрагмента с параметрами для ID и символа актива.
         *
         * @param id Идентификатор актива
         * @param symbol Символ актива
         * @return Новый экземпляр [AssetOverviewFragment]
         */
        fun newInstance(id: String, symbol: String): AssetOverviewFragment =
            AssetOverviewFragment().also {
                it.arguments = Bundle().apply {
                    putString(ID_KEY, id)
                    putString(SYMBOL_KEY, symbol)
                }
            }
    }
}
