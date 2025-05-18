package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.buy_dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.DialogBuyBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.parent.CustomBottomSheetDialog
import ru.surf.learn2invest.presentation.utils.NoArgException
import ru.surf.learn2invest.presentation.utils.getWithCurrency
import ru.surf.learn2invest.presentation.utils.textListener
import ru.surf.learn2invest.presentation.utils.viewModelCreator
import javax.inject.Inject

/**
 * Диалог покупки актива.
 *
 * Отображает окно покупки актива, позволяя пользователю вводить количество лотов,
 * взаимодействовать с кнопками увеличения/уменьшения лотов, вводить торговый пароль,
 * а также выполнять покупку актива при наличии достаточного баланса.
 */
@AndroidEntryPoint
internal class BuyDialog : CustomBottomSheetDialog() {
    override val dialogTag: String = "buy"

    @Inject
    lateinit var factory: BuyDialogViewModel.Factory

    /**
     * ViewModel для работы с данными о покупке актива.
     * Инициализируется с обязательными параметрами (ID, имя, символ актива).
     */
    private val viewModel by viewModelCreator {
        val id = arguments?.getString(ID_KEY) ?: throw NoArgException(ID_KEY)
        val name = arguments?.getString(NAME_KEY) ?: throw NoArgException(NAME_KEY)
        val symbol = arguments?.getString(SYMBOL_KEY) ?: throw NoArgException(SYMBOL_KEY)
        factory.createViewModel(id, name, symbol)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            updateNavigationBarColor(dialog)
        }
        return dialog
    }

    /**
     * Обновляет цвет навигационной панели в зависимости от темы приложения.
     */
    private fun updateNavigationBarColor(dialog: BottomSheetDialog) {
        val window = dialog.window
        if (window != null) {
            window.navigationBarColor = requireContext().getColor(
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    R.color.sheet_background_dark
                } else {
                    R.color.white
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = DialogBuyBinding.inflate(layoutInflater).also { initListeners(it) }.root

    /**
     * Инициализация обработчиков событий для элементов интерфейса.
     */
    private fun initListeners(binding: DialogBuyBinding) {
        binding.apply {
            buttonBuy.isVisible = false
            buttonBuy.setOnClickListener {
                viewModel.handleEvent(BuyDialogIntent.Buy)
            }

            imageButtonPlus.setOnClickListener {
                viewModel.handleEvent(BuyDialogIntent.PlusLot)
            }

            imageButtonMinus.setOnClickListener {
                viewModel.handleEvent(BuyDialogIntent.MinusLot)
            }

            enteringNumberOfLots.addTextChangedListener(
                textListener(afterTextChanged = {
                    viewModel.handleEvent(
                        BuyDialogIntent.SetLot(
                            binding.enteringNumberOfLots.text.toString().toIntOrNull() ?: 0
                        )
                    )
                })
            )

            if (tradingPassword.isVisible) {
                tradingPasswordTV.addTextChangedListener(
                    textListener(afterTextChanged = {

                        viewModel.handleEvent(
                            BuyDialogIntent.SetTradingPassword(
                                binding.tradingPassword.editText?.text.toString()
                            )
                        )
                    })
                )
            }

            viewLifecycleOwner.lifecycleScope.launchMAIN {
                viewModel.state.collectLatest { state ->
                    val lotsData = state.lotsData
                    val currentPrice = state.currentPrice

                    val fiatBalance = state.profile.fiatBalance
                    if (lotsData.isUpdateTVNeeded) {
                        enteringNumberOfLots.setText("${lotsData.lots}")
                    }
                    if (currentPrice != null) {
                        val willPrice = currentPrice * lotsData.lots
                        when {
                            viewModel.isTrueTradingPasswordOrIsNotDefinedUseCase.invoke(state.tradingPassword) &&
                                    lotsData.lots > 0f && fiatBalance != 0f && willPrice <= fiatBalance -> {
                                buttonBuy.isVisible = true
                                result.text =
                                    "${requireContext().getString(R.string.itog)} ${willPrice.getWithCurrency()}"
                            }

                            willPrice > fiatBalance || fiatBalance == 0f -> {
                                buttonBuy.isVisible = false
                                result.text =
                                    requireContext().getString(R.string.not_enough_money_for_buy)
                            }

                            else -> {
                                buttonBuy.isVisible = false
                                result.text = ""
                            }
                        }


                    }

                    val maxQuantity = currentPrice?.let { maxQuantity(it, fiatBalance) }
                    maxQuantityNumber.text = "$maxQuantity"
                    imageButtonPlus.isVisible = maxQuantity?.let { (lotsData.lots < it) } ?: false
                    imageButtonMinus.isVisible = lotsData.lots > 0
                    priceNumber.text = state.currentPrice?.getWithCurrency()
                    balanceNum.text = fiatBalance.getWithCurrency()
                    enteringNumberOfLots.isEnabled = fiatBalance != 0f
                    tradingPassword.isVisible = state.profile.tradingPasswordHash != null &&
                            fiatBalance != 0f
                }
            }
            viewLifecycleOwner.lifecycleScope.launchMAIN {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        BuyDialogEffect.Dismiss -> {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.handleEvent(BuyDialogIntent.StopUpdatingPriceFLow)
    }

    /**
     * Рассчитывает максимальное количество лотов, доступных для покупки.
     */
    private fun maxQuantity(price: Float, balance: Float): Int = (balance / price).toInt()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.handleEvent(BuyDialogIntent.SetupAssetIfInDbAndStartUpdatingPriceFLow)
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val NAME_KEY = "NAME_KEY"
        private const val SYMBOL_KEY = "SYMBOL_KEY"

        /**
         * Создаёт новый экземпляр диалога с переданными параметрами актива.
         */
        fun newInstance(id: String, name: String, symbol: String) = BuyDialog().apply {
            arguments = bundleOf(ID_KEY to id, NAME_KEY to name, SYMBOL_KEY to symbol)
        }
    }
}