package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.sell_dialog

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
import ru.surf.learn2invest.domain.TransactionsType
import ru.surf.learn2invest.domain.cryptography.usecase.IsTrueTradingPasswordOrIsNotDefinedUseCase
import ru.surf.learn2invest.domain.database.usecase.DeleteAssetInvestUseCase
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
 * ViewModel для управления логикой диалога продажи актива.
 *
 * Этот ViewModel отвечает за управление состоянием продажи актива, включая цену, количество лотов и торговый пароль.
 * Он также управляет обновлением данных актива и выполнением операций продажи.
 *
 * @param settingsManager Менеджер профиля пользователя.
 * @param insertTransactionUseCase UseCase для добавления транзакции.
 * @param insertAssetInvestUseCase UseCase для добавление актива.
 * @param deleteAssetInvestUseCase UseCase для удаления активов из инвестиций.
 * @param getCoinReviewUseCase UseCase для получения информации о текущей цене актива.
 * @param getBySymbolAssetInvestUseCase UseCase для получения активов по символу.
 * @param isTrueTradingPasswordOrIsNotDefinedUseCase UseCase для проверки торгового пароля.
 * @param id Идентификатор актива.
 * @param name Название актива.
 * @param symbol Символ актива.
 */
internal class SellDialogViewModel @AssistedInject constructor(
    private val settingsManager: SettingsManager,
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val insertAssetInvestUseCase: InsertAssetInvestUseCase,
    private val deleteAssetInvestUseCase: DeleteAssetInvestUseCase,
    private val getCoinReviewUseCase: GetCoinReviewUseCase,
    private val getBySymbolAssetInvestUseCase: GetBySymbolAssetInvestUseCase,
    val isTrueTradingPasswordOrIsNotDefinedUseCase: IsTrueTradingPasswordOrIsNotDefinedUseCase,
    @Assisted("id") val id: String,
    @Assisted("name") val name: String,
    @Assisted("symbol") val symbol: String,
) : ViewModel() {
    private val profile = settingsManager.settingsFlow

    /**
     * Задержка для обновления реальной цены актива.
     */
    private var realTimeUpdateJob: Job? = null

    private val _effects = MutableSharedFlow<SellDialogEffect>()
    val effects = _effects.asSharedFlow()

    /**
     * Состояние диалога продажи, которое объединяет информацию о лотах, торговом пароле и активе.
     */
    private val _state = MutableStateFlow(
        SellDialogState(
            coin = AssetInvest(
                name = name, symbol = symbol, coinPrice = 0f, amount = 0, assetID = id
            ), settings = profile.value
        )
    )
    val stateFlow = _state.asStateFlow()
    fun handleEvent(intent: SellDialogIntent) {
        viewModelScope.launchIO {
            when (intent) {
                SellDialogIntent.MinusLot -> {
                    minusLot()
                }

                SellDialogIntent.PlusLot -> {
                    plusLot()
                }

                SellDialogIntent.Sell -> {
                    sell()
                    _effects.emit(SellDialogEffect.Dismiss)
                }

                is SellDialogIntent.SetLot -> {
                    setLot(intent.lots)
                }

                is SellDialogIntent.SetTradingPassword -> {
                    setTradingPassword(intent.password)
                }

                SellDialogIntent.SetupAssetIfInDbAndStartUpdatingPriceFLow -> {
                    setAssetIfInDB()
                    startUpdatingPriceFLow()
                }

                SellDialogIntent.StopUpdatingPriceFLow -> {
                    stopUpdatingPriceFlow()
                }
            }
        }
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
        if (_state.value.lotsData.lots > 0) _state.update {
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
     * Устанавливает торговый пароль
     * @param password Введенный пользователем торговый пароль
     */
    private fun setTradingPassword(password: String) {
        _state.update { it.copy(tradingPassword = password) }
    }

    /**
     * Выполняет операцию продажи актива.
     */
    private suspend fun sell() {
        val state = _state.value
        val coin = state.coin
        val amountCurrent = state.lotsData.lots
        val price = state.currentPrice as Float
        settingsManager.update {
            it.copy(fiatBalance = it.fiatBalance + price * amountCurrent)
        }

        coin.apply {
            // Обновление истории транзакций
            insertTransactionUseCase(
                Transaction(
                    coinID = assetID,
                    name = name,
                    symbol = symbol,
                    coinPrice = price,
                    dealPrice = price * amountCurrent,
                    amount = amountCurrent,
                    transactionType = TransactionsType.Sell
                )
            )
        }

        // Обновление портфеля
        if (amountCurrent < coin.amount) {
            insertAssetInvestUseCase(
                coin.copy(
                    coinPrice = (coin.coinPrice * coin.amount - amountCurrent * price) / (coin.amount - amountCurrent),
                    amount = coin.amount - amountCurrent
                )
            )
        } else deleteAssetInvestUseCase(coin)
    }

    /**
     * Запускает обновление цены актива каждые 5 секунд.
     */
    private fun startUpdatingPriceFLow() {
        realTimeUpdateJob = viewModelScope.launchIO {
            while (true) {
                when (val result = getCoinReviewUseCase.invoke(_state.value.coin.assetID)) {
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
     * Останавливает обновление цены актива.
     */
    private fun stopUpdatingPriceFlow() {
        realTimeUpdateJob?.cancel()
        realTimeUpdateJob = null
    }

    /**
     * Устанавливает актив, если он найден в базе данных.
     */
    private suspend fun setAssetIfInDB() {
        getBySymbolAssetInvestUseCase.invoke(symbol = symbol).first()?.let { assetInvest ->
            _state.update {
                it.copy(coin = assetInvest)
            }
        }
    }

    /**
     * Фабрика для создания экземпляров [SellDialogViewModel].
     */
    @AssistedFactory
    interface Factory {
        /**
         * Создает экземпляр [SellDialogViewModel] с заданными параметрами.
         *
         * @param id Идентификатор актива.
         * @param name Название актива.
         * @param symbol Символ актива.
         * @return Экземпляр [SellDialogViewModel].
         */
        fun createViewModel(
            @Assisted("id") id: String,
            @Assisted("name") name: String,
            @Assisted("symbol") symbol: String,
        ): SellDialogViewModel
    }
}
