package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_overview

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.database.usecase.GetBySymbolAssetInvestUseCase
import ru.surf.learn2invest.domain.domain_models.AssetInvest
import ru.surf.learn2invest.domain.domain_models.AugmentedCoinReview
import ru.surf.learn2invest.domain.network.ResponseResult
import ru.surf.learn2invest.domain.network.usecase.GetCoinHistoryUseCase
import ru.surf.learn2invest.domain.network.usecase.GetCoinReviewUseCase
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.presentation.ui.components.chart.Last7DaysFormatter
import ru.surf.learn2invest.presentation.ui.components.chart.LineChartHelper
import ru.surf.learn2invest.presentation.utils.formatAsPrice
import ru.surf.learn2invest.presentation.utils.getWithCurrency
import ru.surf.learn2invest.presentation.utils.priceChangesStr

/**
 * [ViewModel] для фрагмента [AssetOverviewFragment], отвечающий за логику получения и обновления данных актива.
 * Загружает данные для графика, рыночной капитализации и информации о монете.
 */
internal class AssetOverViewFragmentViewModel @AssistedInject constructor(
    private val getCoinHistoryUseCase: GetCoinHistoryUseCase,
    private val getCoinReviewUseCase: GetCoinReviewUseCase,
    getBySymbolAssetInvestUseCase: GetBySymbolAssetInvestUseCase,
    @Assisted("id") private val id: String,
    @Assisted("name") private val name: String,
    @Assisted("symbol") private val symbol: String,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var data = listOf<Entry>()
    private var chartHelper: LineChartHelper? = null
    private var realTimeUpdateJob: Job? = null

    private val _state = MutableStateFlow(AssetOverviewState())
    val state = _state.asStateFlow()
    private val _effects = MutableSharedFlow<AssetOverviewFragmentEffect>()
    val effects = _effects.asSharedFlow()
    fun handleIntent(intent: AssetOverviewFragmentIntent) {
        viewModelScope.launchIO {
            when (intent) {
                is AssetOverviewFragmentIntent.SetupChartAndLoadChartData -> {
                    chartHelper = LineChartHelper(context, Last7DaysFormatter())
                    chartHelper?.setupChart(intent.lineChart)
                    loadChartData()
                }

                AssetOverviewFragmentIntent.StartUpdatingPriceFLow -> {
                    startRealTimeUpdate()
                }

                AssetOverviewFragmentIntent.StopUpdatingPriceFLow -> {
                    stopRealTimeUpdateJob()
                }

                AssetOverviewFragmentIntent.BuyAsset -> _effects.emit(
                    AssetOverviewFragmentEffect.OpenBuyDialog(
                        id,
                        name,
                        symbol
                    )
                )

                AssetOverviewFragmentIntent.SellAsset -> _effects.emit(
                    AssetOverviewFragmentEffect.OpenSellDialog(
                        id,
                        name,
                        symbol
                    )
                )
            }
        }
    }

    init {
        viewModelScope.launchIO {
            getBySymbolAssetInvestUseCase.invoke(symbol).collect { assetInvest ->
                _state.update {
                    it.copy(
                        coin = assetInvest ?: AssetInvest(
                            assetID = id,
                            name = name,
                            symbol = symbol,
                            coinPrice = 0f,
                            amount = 0
                        )
                    )
                }
                updateStateDependsOnPrice()
            }
        }
    }


    /**
     * Обновление данных для графика и отображение рыночной капитализации и цены.
     */
    private fun updateChartData(coinResponse: ResponseResult.Success<AugmentedCoinReview>) {
        data = if (data.isNotEmpty()) {
            data.subList(0, data.lastIndex)
                .plus(Entry(data.size.toFloat(), coinResponse.value.priceUsd))
        } else {
            data.plus(Entry(0f, coinResponse.value.priceUsd))
        }
        _state.update {
            it.copy(
                marketCap = coinResponse.value.marketCapUsd,
                price = coinResponse.value.priceUsd
            )
        }
        updateStateDependsOnPrice()

        chartHelper?.updateData(data)
    }

    private fun updateStateDependsOnPrice() {
        _state.update { state ->
            val asset = state.coin
            state.copy(
                finResult = (if (asset != null && asset.amount != 0 && state.price != null) {
                    FinResult(asset, state.price)
                } else null),
                coinPriceChangesResult = if (asset != null && state.price != null) {
                    priceChangesStr(
                        asset,
                        state.price
                    )
                } else null,
                coinCostResult = if (asset != null && state.price != null) {
                    (state.price * asset.amount).formatAsPrice(2).getWithCurrency()
                } else null,
                price = state.price,
            ).also {
                Log.e("state", "$it")
            }
        }

    }

    /**
     * Загрузка исторических данных для актива и обновление графика.
     */
    private fun loadChartData() {
        viewModelScope.launchIO {
            val response = getCoinHistoryUseCase(id)
            if (response is ResponseResult.Success) {
                data = response.value.mapIndexed { index, coinPriceResponse ->
                    Entry(index.toFloat(), coinPriceResponse.priceUsd)
                }.toMutableList()
                val coinResponse = getCoinReviewUseCase(id)
                if (coinResponse is ResponseResult.Success) {
                    updateChartData(coinResponse)
                }
            }
        }
    }

    /**
     * Запуск реального обновления данных с интервалом в 5 секунд.
     */
    private fun startRealTimeUpdate() {
        realTimeUpdateJob = viewModelScope.launchIO {
            while (true) {
                val result = getCoinReviewUseCase(id)
                Log.e("success", "$result")
                if (result is ResponseResult.Success) {
                    Log.e("success", "${result.value}")
                    updateChartData(result)
                    _state.update { it.copy(price = result.value.priceUsd) }
                    updateStateDependsOnPrice()
                }
                delay(5000)
            }
        }
    }

    /**
     * Остановка работы с реальными данными.
     */
    private fun stopRealTimeUpdateJob() {
        realTimeUpdateJob?.cancel()
        realTimeUpdateJob = null
    }

    @AssistedFactory
    interface Factory {
        /**
         * Создание экземпляра [AssetOverViewFragmentViewModel]
         *
         * @param id Идентификатор актива
         * @param symbol Символ актива
         * @return Новый экземпляр [AssetOverViewFragmentViewModel]
         */
        fun createAssetOverViewFragmentViewModel(
            @Assisted("id") id: String,
            @Assisted("name") name: String,
            @Assisted("symbol") symbol: String,
        ): AssetOverViewFragmentViewModel
    }
}
