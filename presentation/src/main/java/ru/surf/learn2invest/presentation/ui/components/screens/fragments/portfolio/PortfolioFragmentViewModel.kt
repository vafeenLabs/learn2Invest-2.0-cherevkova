package ru.surf.learn2invest.presentation.ui.components.screens.fragments.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.database.usecase.GetAllAssetBalanceHistoryUseCase
import ru.surf.learn2invest.domain.database.usecase.GetAllAssetInvestUseCase
import ru.surf.learn2invest.domain.database.usecase.GetBySymbolAssetInvestUseCase
import ru.surf.learn2invest.domain.database.usecase.InsertAssetBalanceHistoryUseCase
import ru.surf.learn2invest.domain.database.usecase.InsertByLimitAssetBalanceHistoryUseCase
import ru.surf.learn2invest.domain.domain_models.AssetBalanceHistory
import ru.surf.learn2invest.domain.domain_models.AssetInvest
import ru.surf.learn2invest.domain.network.ResponseResult
import ru.surf.learn2invest.domain.network.usecase.GetCoinReviewUseCase
import ru.surf.learn2invest.domain.services.ProfileManager
import ru.surf.learn2invest.domain.utils.launchIO
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel для управления состоянием портфеля пользователя в приложении.
 * Отвечает за получение данных о балансе активов, их изменениях, а также за обновление
 * данных о ценах активов и вычисление изменений в портфеле.
 *
 * @param profileManager Менеджер профиля пользователя, который предоставляет доступ к данным
 *                       профиля, таким как баланс активов и фиатный баланс.
 * @param getAllAssetInvestUseCase UseCase для получения всех активов, в которые был сделан вклад.
 * @param getAllAssetBalanceHistoryUseCase UseCase для получения истории баланса активов пользователя.
 * @param insertAssetBalanceHistoryUseCase UseCase для вставки данных о балансе активов в историю.
 * @param insertByLimitAssetBalanceHistoryUseCase UseCase для вставки данных о балансе активов с ограничением.
 * @param getCoinReviewUseCase UseCase для получения данных о текущей цене актива.
 * @param getBySymbolAssetInvestUseCase UseCase для получения информации о конкретном активе по его символу.
 */
@HiltViewModel
internal class PortfolioFragmentViewModel @Inject constructor(
    private val profileManager: ProfileManager,
    private val getAllAssetInvestUseCase: GetAllAssetInvestUseCase,
    private val getAllAssetBalanceHistoryUseCase: GetAllAssetBalanceHistoryUseCase,
    private val insertAssetBalanceHistoryUseCase: InsertAssetBalanceHistoryUseCase,
    private val insertByLimitAssetBalanceHistoryUseCase: InsertByLimitAssetBalanceHistoryUseCase,
    private val getCoinReviewUseCase: GetCoinReviewUseCase,
    private val getBySymbolAssetInvestUseCase: GetBySymbolAssetInvestUseCase,
) : ViewModel() {
    // Константа для ограничения размера истории баланса
    private val historyLimitSize = 7

    private val _state = MutableStateFlow(PortfolioFragmentState())
    val state = _state.asStateFlow()
    private val _effects = MutableSharedFlow<PortfolioFragmentEffect>()
    val effects = _effects.asSharedFlow()
    fun handleIntent(intent: PortfolioFragmentIntent) {
        viewModelScope.launchIO {
            when (intent) {
                PortfolioFragmentIntent.StartRealtimeUpdate -> startUpdatingPriceFLow()
                PortfolioFragmentIntent.StopRealtimeUpdate -> stopUpdatingPriceFlow()
                is PortfolioFragmentIntent.StartAssetReviewActivity -> startAssetReviewActivity(
                    id = intent.id, name = intent.name, symbol = intent.symbol
                )

                PortfolioFragmentIntent.OpenDrawer -> openDrawer()
                PortfolioFragmentIntent.CloseDrawer -> closeDrawer()
                is PortfolioFragmentIntent.MailTo -> mailTo(intent.mail)
                is PortfolioFragmentIntent.OpenLink -> openLink(intent.link)
                PortfolioFragmentIntent.OpenRefillAccountDialog -> openRefillAccountDialog()
            }
        }
    }

    private suspend fun openRefillAccountDialog() =
        _effects.emit(PortfolioFragmentEffect.OpenRefillAccountDialog)

    private suspend fun openDrawer() = _effects.emit(PortfolioFragmentEffect.OpenDrawer)

    private suspend fun closeDrawer() = _effects.emit(PortfolioFragmentEffect.CloseDrawer)

    private suspend fun mailTo(mail: String) = _effects.emit(PortfolioFragmentEffect.MailTo(mail))

    private suspend fun openLink(link: String) =
        _effects.emit(PortfolioFragmentEffect.OpenLink(link))

    init {
        viewModelScope.launchIO {
            getAllAssetInvestUseCase().flowOn(Dispatchers.IO)
                .onEach { assets -> loadPriceChanges(assets) }.collect { assets ->
                    _state.update { it.copy(assets = assets) }
                }
        }
    }

    init {
        viewModelScope.launchIO {
            profileManager.profileFlow.collectLatest { profile ->
                _state.update {
                    it.copy(fiatBalance = profile.fiatBalance, assetBalance = profile.assetBalance)
                }
            }
        }
    }

    private suspend fun startAssetReviewActivity(
        id: String, name: String, symbol: String
    ) {
        _effects.emit(
            PortfolioFragmentEffect.StartAssetReviewActivity(
                id = id, name = name, symbol = symbol
            )
        )
    }

    // Задача для обновления данных в реальном времени
    private var realTimeUpdateJob: Job? = null

    /**
     * Обновляет данные портфеля, включая баланс и цены активов.
     */
    private suspend fun refreshData() {
        checkAndUpdateBalanceHistory()
        loadPriceChanges(getAllAssetInvestUseCase().first())
    }

    /**
     * Получает даты всех записей в истории баланса активов.
     *
     * @return Список дат всех записей истории баланса активов.
     */
    init {
        viewModelScope.launchIO {
            getAllAssetBalanceHistoryUseCase().map { history ->
                history.map { it.date }
            }.collect { dates ->
                _state.update { it.copy(dates = dates) }
            }
        }
    }

    /**
     * Запускает периодическое обновление данных о ценах активов.
     */
    private fun startUpdatingPriceFLow() {
        realTimeUpdateJob = viewModelScope.launchIO {
            while (true) {
                refreshData()
                delay(5000)
            }
        }
    }

    /**
     * Останавливает обновление данных о ценах активов.
     */
    private fun stopUpdatingPriceFlow() {
        realTimeUpdateJob?.cancel()
        realTimeUpdateJob = null
    }

    init {
        viewModelScope.launchIO {
            getAllAssetBalanceHistoryUseCase().collect {
                _state.update { state ->
                    state.copy(chartData = it.mapIndexed { index, assetBalanceHistory ->
                        Entry(index.toFloat(), assetBalanceHistory.assetBalance)
                    })
                }
            }
        }
    }

    /**
     * Проверяет и обновляет историю баланса активов, если необходимо.
     */
    private suspend fun checkAndUpdateBalanceHistory() {
        val state = _state.value
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayDate = today.time

        val todayBalanceHistory = getAllAssetBalanceHistoryUseCase().first().find {
            val historyDate = Calendar.getInstance().apply {
                time = it.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            historyDate.time == todayDate
        }

        val totalBalance = state.assetBalance + state.fiatBalance

        if (todayBalanceHistory != null) {
            insertAssetBalanceHistoryUseCase(
                todayBalanceHistory.copy(
                    assetBalance = totalBalance
                )
            )
        } else {
            insertByLimitAssetBalanceHistoryUseCase(
                historyLimitSize, AssetBalanceHistory(assetBalance = totalBalance, date = todayDate)
            )
        }
    }

    /**
     * Загружает изменения цен для всех активов.
     *
     * @param assets Список активов для которых необходимо обновить данные о ценах.
     */
    private suspend fun loadPriceChanges(assets: List<AssetInvest>) {
        val priceChanges = mutableMapOf<String, Float>()
        var totalCurrentValue = 0f
        var initialInvestment = 0f
        var currentPrice: Float
        for (asset in assets) {
            val response = getCoinReviewUseCase(asset.assetID)
            if (response is ResponseResult.Success) {
                currentPrice = response.value.priceUsd
                priceChanges[asset.symbol] = currentPrice
                totalCurrentValue += currentPrice * asset.amount

                getBySymbolAssetInvestUseCase(asset.symbol).first()?.let {
                    initialInvestment += it.coinPrice * asset.amount
                }
            }
        }
        profileManager.updateProfile {
            it.copy(assetBalance = totalCurrentValue)
        }
        _state.update { it.copy(priceChanges = priceChanges) }
        calculatePortfolioChangePercentage(totalCurrentValue, initialInvestment)
    }

    /**
     * Вычисляет процентное изменение портфеля.
     *
     * @param totalCurrentValue Общая текущая стоимость активов.
     * @param initialInvestment Начальная сумма инвестиций.
     */
    private fun calculatePortfolioChangePercentage(
        totalCurrentValue: Float, initialInvestment: Float
    ) {
        val assetBalance = _state.value.assetBalance
        if (initialInvestment != 0f) {
            val portfolioChangePercentage =
                ((totalCurrentValue + assetBalance) / (initialInvestment + assetBalance) - 1) * 100
            val roundedPercentage =
                BigDecimal(portfolioChangePercentage.toDouble()).setScale(2, RoundingMode.HALF_UP)
                    .toFloat()
            _state.update { it.copy(portfolioChangePercentage = roundedPercentage) }
        } else {
            _state.update { it.copy(portfolioChangePercentage = 0f) }
        }
    }
}
