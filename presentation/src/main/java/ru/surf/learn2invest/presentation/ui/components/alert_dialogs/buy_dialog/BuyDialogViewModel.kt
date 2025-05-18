package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.buy_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.surf.learn2invest.domain.TransactionsType
import ru.surf.learn2invest.domain.cryptography.usecase.IsTrueTradingPasswordOrIsNotDefinedUseCase
import ru.surf.learn2invest.domain.database.usecase.GetBySymbolAssetInvestUseCase
import ru.surf.learn2invest.domain.database.usecase.InsertAssetInvestUseCase
import ru.surf.learn2invest.domain.database.usecase.InsertTransactionUseCase
import ru.surf.learn2invest.domain.domain_models.AssetInvest
import ru.surf.learn2invest.domain.domain_models.Transaction
import ru.surf.learn2invest.domain.network.ResponseResult
import ru.surf.learn2invest.domain.network.usecase.GetCoinReviewUseCase
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.common.LotsData

/**
 * ViewModel для диалогового окна покупки криптовалюты.
 * Отвечает за получение текущей цены актива, управление балансом и добавление транзакций.
 * @property settingsManager Менеджер профиля пользователя
 * @property insertTransactionUseCase UseCase для добавления транзакции
 * @property insertAssetInvestUseCase UseCase для обновления данных об инвестициях в актив
 * @property getCoinReviewUseCase UseCase для получения текущей цены актива
 * @property getBySymbolAssetInvestUseCase UseCase для получения информации об активе по символу
 * @property isTrueTradingPasswordOrIsNotDefinedUseCase UseCase для проверки пароля для торговли
 * @property id Идентификатор актива
 * @property name Название актива
 * @property symbol Символ актива (тикер)
 */
internal class BuyDialogViewModel @AssistedInject constructor(
    private val settingsManager: SettingsManager,
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val insertAssetInvestUseCase: InsertAssetInvestUseCase,
    private val getCoinReviewUseCase: GetCoinReviewUseCase,
    private val getBySymbolAssetInvestUseCase: GetBySymbolAssetInvestUseCase,
    val isTrueTradingPasswordOrIsNotDefinedUseCase: IsTrueTradingPasswordOrIsNotDefinedUseCase,
    @Assisted("id") val id: String,
    @Assisted("name") val name: String,
    @Assisted("symbol") val symbol: String,
) : ViewModel() {
    private val profile = settingsManager.settingsFlow

    /**
     * Задача для обновления цены актива в реальном времени
     */
    private var realTimeUpdateJob: Job? = null
    private val _effects = MutableSharedFlow<BuyDialogEffect>()
    val effects = _effects.asSharedFlow()

    /**
     * Комбинированный StateFlow, содержащий актуальное состояние покупки
     */
    private val _state = MutableStateFlow(
        BuyDialogState(
            coin = AssetInvest(
                name = name,
                symbol = symbol,
                coinPrice = 0f,
                amount = 0,
                assetID = id
            ),
            settings = profile.value
        )
    )
    val state = _state.asStateFlow()
    fun handleEvent(intent: BuyDialogIntent) {
        viewModelScope.launchIO {
            when (intent) {
                BuyDialogIntent.Buy -> {
                    buy()
                    _effects.emit(BuyDialogEffect.Dismiss)
                }

                BuyDialogIntent.MinusLot -> minusLot()
                BuyDialogIntent.PlusLot -> plusLot()
                is BuyDialogIntent.SetLot -> setLot(intent.lots)
                is BuyDialogIntent.SetTradingPassword -> setTradingPassword(intent.password)
                BuyDialogIntent.SetupAssetIfInDbAndStartUpdatingPriceFLow -> {
                    setAssetIfInDB()
                    startUpdatingPriceFLow()
                }

                BuyDialogIntent.StopUpdatingPriceFLow -> stopUpdatingPriceFlow()
            }
        }
    }

    /**
     * Запускает обновление цены актива в реальном времени с интервалом 5 секунд
     */
    private fun startUpdatingPriceFLow() {
        realTimeUpdateJob = viewModelScope.launch {
            while (true) {
                when (val result = getCoinReviewUseCase.invoke(state.value.coin.assetID)) {
                    is ResponseResult.Success -> {
                        _state.update {
                            it.copy(currentPrice = result.value.priceUsd)
                        }
                    }

                    is ResponseResult.Error -> {}
                }
                delay(5000)
            }
        }
    }

    /**
     * Проверяет, есть ли актив в базе данных, и устанавливает его, если найден
     */
    private suspend fun setAssetIfInDB() {
        getBySymbolAssetInvestUseCase.invoke(symbol = symbol).first()?.let { asset ->
            _state.update {
                it.copy(coin = asset)
            }
        }
    }

    /**
     * Останавливает обновление цены актива
     */
    private fun stopUpdatingPriceFlow() {
        realTimeUpdateJob?.cancel()
        realTimeUpdateJob = null
    }

    /**
     * Устанавливает торговый пароль
     * @param password Введенный пользователем торговый пароль
     */
    private fun setTradingPassword(password: String) {
        _state.update { it.copy(tradingPassword = password) }
    }

    /**
     * Увеличивает количество лотов на 1
     */
    private fun plusLot() {
        _state.update {
            it.copy(lotsData = LotsData(it.lotsData.lots + 1))
        }
    }

    /**
     * Уменьшает количество лотов на 1, если их больше 0
     */
    private fun minusLot() {
        if (_state.value.lotsData.lots > 0)
            _state.update {
                it.copy(lotsData = LotsData(it.lotsData.lots - 1))
            }
    }

    /**
     * Устанавливает конкретное количество лотов
     * @param lotsNumber Количество лотов
     */
    private fun setLot(lotsNumber: Int) {
        _state.update {
            it.copy(lotsData = LotsData(lots = lotsNumber, isUpdateTVNeeded = false))
        }
    }

    /**
     * Покупает актив, обновляя баланс пользователя и добавляя транзакцию
     */
    private suspend fun buy() {
        val state = _state.value
        val coin = state.coin
        val price = state.currentPrice ?: return
        val amountCurrent = state.lotsData.lots
        val balance = settingsManager.settingsFlow.value.fiatBalance
        if (balance != 0f && balance > price * amountCurrent) {
            // Обновление баланса
            settingsManager.update {
                it.copy(fiatBalance = balance - price * amountCurrent)
            }
            // Обновление портфеля
            insertAssetInvestUseCase.invoke(
                coin.copy(
                    coinPrice = (coin.coinPrice * coin.amount + amountCurrent * price) /
                            (coin.amount + amountCurrent),
                    amount = coin.amount + amountCurrent
                )
            )
            // Добавление транзакции
            insertTransactionUseCase.invoke(
                Transaction(
                    coinID = coin.assetID,
                    name = name,
                    symbol = symbol,
                    coinPrice = price,
                    dealPrice = price * amountCurrent,
                    amount = amountCurrent,
                    transactionType = TransactionsType.Buy
                )
            )
        }
    }

    /**
     * Фабрика для создания экземпляра ViewModel с передачей параметров
     */
    @AssistedFactory
    interface Factory {
        fun createViewModel(
            @Assisted("id") id: String,
            @Assisted("name") name: String,
            @Assisted("symbol") symbol: String,
        ): BuyDialogViewModel
    }
}


