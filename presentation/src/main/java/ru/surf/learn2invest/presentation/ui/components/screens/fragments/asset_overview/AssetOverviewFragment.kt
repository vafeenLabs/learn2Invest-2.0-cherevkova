package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.FragmentAssetOverviewBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.buy_dialog.BuyDialog
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.sell_dialog.SellDialog
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
            symbol = requireArguments().getString(SYMBOL_KEY) ?: throw NoArgException(SYMBOL_KEY),
            name = requireArguments().getString(NAME_KEY) ?: throw NoArgException(NAME_KEY),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentAssetOverviewBinding.inflate(inflater, container, false)

        // Настройка и загрузка данных для графика
        viewModel.handleIntent(
            AssetOverviewFragmentIntent.SetupChartAndLoadChartData(
                binding.chart
            )
        )


        // Определение темы (темная или светлая)
        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val accentColor =
            getColorRes(if (isDarkTheme) R.color.accent_background_dark else R.color.accent_background)

        // Установка фона для корневого элемента
        binding.coin.root.setCardBackgroundColor(accentColor)

        initListeners(binding)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.handleIntent(AssetOverviewFragmentIntent.StartUpdatingPriceFLow)
    }

    override fun onStop() {
        viewModel.handleIntent(AssetOverviewFragmentIntent.StopUpdatingPriceFLow)
        super.onStop()
    }

    private fun initListeners(binding: FragmentAssetOverviewBinding) {
        // Обработчик кнопки "Купить актив"
        binding.buyAssetBtn.setOnClickListener {
            viewModel.handleIntent(AssetOverviewFragmentIntent.BuyAsset)
        }

        // Обработчик кнопки "Продать актив"
        binding.sellAssetBtn.setOnClickListener {
            viewModel.handleIntent(AssetOverviewFragmentIntent.SellAsset)
        }

        // Сбор и отображение информации о монете
        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                binding.coinItemLayout.isVisible =
                    state.finResult != null && state.coinCostResult != null && state.coinPriceChangesResult != null && (state.coin?.amount != null)
                binding.sellBuyBtns.isVisible = state.price != null
                binding.coin.apply {
                    finResult.text = state.finResult?.formattedStr()
                    state.finResult?.let { finResult.setTextColor(getColorRes(it.color())) }
                    coinsCost.text = state.coinCostResult
                    coinsPrice.text = state.coinPriceChangesResult
                    coinsCount.text = state.coin?.amount?.let { "$it" }
                }
                binding.capitalisation.text = state.marketCap?.let {
                    NumberFormat.getInstance(Locale.US).apply {
                        maximumFractionDigits = 0
                    }.format(it).getWithCurrency()
                }
                binding.price.text = state.price?.formatAsPrice(8)?.getWithCurrency()
            }
        }

        // Обработка сайд эффектов
        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is AssetOverviewFragmentEffect.OpenBuyDialog -> {
                        BuyDialog.newInstance(
                            effect.id,
                            effect.name,
                            effect.symbol
                        )
                            .showDialog(parentFragmentManager)
                    }

                    is AssetOverviewFragmentEffect.OpenSellDialog -> {
                        SellDialog.newInstance(
                            effect.id,
                            effect.name,
                            effect.symbol
                        )
                            .showDialog(parentFragmentManager)
                    }
                }
            }
        }
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val SYMBOL_KEY = "SYMBOL_KEY"
        private const val NAME_KEY = "NAME_KEY"

        /**
         * Создание нового экземпляра фрагмента с параметрами для ID и символа актива.
         *
         * @param id Идентификатор актива
         * @param symbol Символ актива
         * @return Новый экземпляр [AssetOverviewFragment]
         */
        fun newInstance(id: String, name: String, symbol: String): AssetOverviewFragment =
            AssetOverviewFragment().also {
                it.arguments = Bundle().apply {
                    putString(ID_KEY, id)
                    putString(NAME_KEY, name)
                    putString(SYMBOL_KEY, symbol)
                }
            }
    }
}
