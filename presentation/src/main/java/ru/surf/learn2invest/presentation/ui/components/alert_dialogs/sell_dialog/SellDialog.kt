package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.sell_dialog

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
import ru.surf.learn2invest.presentation.databinding.DialogSellBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.parent.CustomBottomSheetDialog
import ru.surf.learn2invest.presentation.utils.NoArgException
import ru.surf.learn2invest.presentation.utils.getWithCurrency
import ru.surf.learn2invest.presentation.utils.textListener
import ru.surf.learn2invest.presentation.utils.viewModelCreator
import javax.inject.Inject

/**
 * Диалог для продажи актива.
 *
 * Этот диалог отображается при попытке пользователя продать актив (например, криптовалюту).
 * Пользователь может выбрать количество лотов для продажи, ввести торговый пароль (если требуется),
 * а также увидеть результат продажи, включая цену и доступное количество.
 */
@AndroidEntryPoint
internal class SellDialog : CustomBottomSheetDialog() {

    /**
     * Тег диалога для идентификации.
     */
    override val dialogTag: String = "sell"

    /**
     * Фабрика для создания ViewModel.
     */
    @Inject
    lateinit var factory: SellDialogViewModel.Factory

    /**
     * ViewModel, который управляет логикой продажи актива.
     */
    private val viewModel by viewModelCreator {
        val id = arguments?.getString(ID_KEY) ?: throw NoArgException(ID_KEY)
        val name = arguments?.getString(NAME_KEY) ?: throw NoArgException(NAME_KEY)
        val symbol = arguments?.getString(SYMBOL_KEY) ?: throw NoArgException(SYMBOL_KEY)
        factory.createViewModel(id, name, symbol)
    }

    /**
     * Инициализирует слушателей для кнопок и других элементов управления в диалоге.
     *
     * Этот метод настраивает:
     * - подписку на изменения профиля пользователя для отображения баланса.
     * - обработчики для кнопок увеличения и уменьшения количества лотов.
     * - обработчик изменения текста для ввода количества лотов и торгового пароля.
     * - обновление UI в зависимости от состояния продажи.
     */
    private fun initListeners(binding: DialogSellBinding) {
        binding.apply {

            // Деактивация кнопки продажи по умолчанию
            buttonSell.isVisible = false
            buttonSell.setOnClickListener {
                viewModel.handleEvent(SellDialogIntent.Sell)
            }

            // Обработчики для кнопок изменения количества лотов
            imageButtonPlus.setOnClickListener {
                viewModel.handleEvent(SellDialogIntent.PlusLot)
            }
            imageButtonMinus.setOnClickListener {
                viewModel.handleEvent(SellDialogIntent.MinusLot)
            }

            // Обработчик изменения текста для количества лотов
            enteringNumberOfLots.addTextChangedListener(
                textListener(afterTextChanged = {
                    viewModel.handleEvent(
                        SellDialogIntent.SetLot(
                            binding.enteringNumberOfLots.text.toString().toIntOrNull() ?: 0
                        )
                    )
                })
            )

            // Обработчик изменения текста для торгового пароля
            tradingPasswordTV.addTextChangedListener(
                textListener(afterTextChanged = {
                    viewModel.handleEvent(
                        SellDialogIntent.SetTradingPassword(
                            binding.tradingPassword.editText?.text.toString()
                        )
                    )
                })
            )

            // Подписка на изменения состояния и обновление UI
            viewLifecycleOwner.lifecycleScope.launchMAIN {
                viewModel.stateFlow.collectLatest { state ->
                    val lotsData = state.lotsData
                    tradingPassword.isVisible =
                        state.settings.tradingPasswordHash != null && state.coin.amount > 0
                    balanceNum.text = state.settings.fiatBalance.getWithCurrency()
                    val resultPrice = state.currentPrice?.let { it * state.lotsData.lots }
                    when {
                        state.coin.amount == 0 -> {
                            buttonSell.isVisible = false
                            result.text = requireContext().getString(R.string.no_asset_for_sale)
                        }

                        state.coin.amount > 0f && lotsData.lots in 1..state.coin.amount -> {
                            buttonSell.isVisible =
                                viewModel.isTrueTradingPasswordOrIsNotDefinedUseCase
                                    .invoke(password = tradingPasswordTV.text.toString())
                            resultPrice?.let {
                                result.text =
                                    "${requireContext().getString(R.string.itog)} ${it.getWithCurrency()}"
                            }
                        }

                        else -> {
                            buttonSell.isVisible = false
                            result.text = ""
                        }
                    }
                    imageButtonPlus.isVisible = lotsData.lots < state.coin.amount
                    imageButtonMinus.isVisible = lotsData.lots > 0

                    binding.priceNumber.text = state.currentPrice?.getWithCurrency()
                    binding.haveQuantityNumber.text = "${state.coin.amount}"

                    if (lotsData.isUpdateTVNeeded) binding.enteringNumberOfLots.setText("${lotsData.lots}")
                }
            }
            viewLifecycleOwner.lifecycleScope.launchMAIN {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        SellDialogEffect.Dismiss -> {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.handleEvent(SellDialogIntent.SetupAssetIfInDbAndStartUpdatingPriceFLow)
    }

    override fun onDetach() {
        viewModel.handleEvent(SellDialogIntent.StopUpdatingPriceFLow)
        super.onDetach()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DialogSellBinding.inflate(layoutInflater)
        initListeners(binding)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
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
        return dialog
    }


    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val NAME_KEY = "NAME_KEY"
        private const val SYMBOL_KEY = "SYMBOL_KEY"

        /**
         * Создает новый экземпляр диалога продажи с указанными параметрами.
         *
         * @param id ID актива.
         * @param name Имя актива.
         * @param symbol Символ актива.
         * @return Новый экземпляр диалога продажи.
         */
        fun newInstance(
            id: String,
            name: String,
            symbol: String,
        ) = SellDialog().apply {
            arguments = bundleOf(ID_KEY to id, NAME_KEY to name, SYMBOL_KEY to symbol)
        }
    }
}
