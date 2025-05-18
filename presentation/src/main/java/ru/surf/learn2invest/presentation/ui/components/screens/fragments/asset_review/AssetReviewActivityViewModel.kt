package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_review

import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.surf.learn2invest.domain.services.coin_icon_loader.CoinIconLoader
import ru.surf.learn2invest.domain.services.coin_icon_loader.usecase.LoadCoinIconUseCase
import ru.surf.learn2invest.domain.utils.launchIO

/**
 * ViewModel для [AssetReviewActivity], управляющий загрузкой иконки актива.
 * Хранит и управляет запросом на загрузку иконки.
 */
internal class AssetReviewActivityViewModel @AssistedInject constructor(
    private val loadCoinIconUseCase: LoadCoinIconUseCase,
    @Assisted("id") private val id: String,
    @Assisted("name") private val name: String,
    @Assisted("symbol") private val symbol: String,
) :
    ViewModel() {
    private var imageLoaderRequest: CoinIconLoader.Request? = null
    private val _state = MutableStateFlow(
        AssetReviewActivityState(name = name, symbol = symbol, id = id)
    )
    val state = _state.asStateFlow()
    private val _effects = MutableSharedFlow<AssetReviewActivityEffect>()
    val effects = _effects.asSharedFlow()
    fun handleIntent(intent: AssetReviewActivityIntent) {
        viewModelScope.launchIO {
            when (intent) {
                AssetReviewActivityIntent.GoBack -> {
                    _effects.emit(AssetReviewActivityEffect.GoBack)
                }

                AssetReviewActivityIntent.OpenAssetOverViewFragment -> {
                    _state.update {
                        it.copy(
                            isOverviewSelected = true,
                        )
                    }
                }

                AssetReviewActivityIntent.OpenSubHistoryFragment -> {
                    _state.update {
                        it.copy(
                            isOverviewSelected = false,
                        )
                    }
                }

                AssetReviewActivityIntent.CancelLoadingImage -> {
                    cancelLoadingImage()
                }

                is AssetReviewActivityIntent.LoadIcon -> {
                    loadImage(intent.imageView, symbol)
                }
            }
        }
    }

    /**
     * Загружает иконку актива и отображает её в переданном ImageView.
     */
    private fun loadImage(imageView: ImageView, symbol: String) {
        imageLoaderRequest = loadCoinIconUseCase.invoke(imageView, symbol)
    }

    /**
     * Отменяет текущую загрузку иконки.
     */
    private fun cancelLoadingImage() {
        imageLoaderRequest?.cancel()
    }

    @AssistedFactory
    interface Factory {
        /**
         * Создание экземпляра [AssetReviewActivityViewModel]
         *
         * @param id Идентификатор актива
         * @param symbol Символ актива
         * @return Новый экземпляр [AssetReviewActivityViewModel]
         */
        fun createAssetReviewActivityViewModel(
            @Assisted("id") id: String,
            @Assisted("name") name: String,
            @Assisted("symbol") symbol: String,
        ): AssetReviewActivityViewModel
    }
}
