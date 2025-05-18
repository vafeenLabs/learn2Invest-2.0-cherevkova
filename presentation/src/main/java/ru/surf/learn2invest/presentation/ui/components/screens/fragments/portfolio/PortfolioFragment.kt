package ru.surf.learn2invest.presentation.ui.components.screens.fragments.portfolio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.FragmentPortfolioBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.refill_account_dialog.RefillAccountDialog
import ru.surf.learn2invest.presentation.ui.components.chart.AssetBalanceHistoryFormatter
import ru.surf.learn2invest.presentation.ui.components.chart.LineChartHelper
import ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_review.AssetReviewActivity
import ru.surf.learn2invest.presentation.ui.components.screens.fragments.common.BaseResFragment
import ru.surf.learn2invest.presentation.utils.DevStrLink
import ru.surf.learn2invest.presentation.utils.getVersionName
import ru.surf.learn2invest.presentation.utils.getWithCurrency
import ru.surf.learn2invest.presentation.utils.setStatusBarColor
import java.util.Locale
import javax.inject.Inject

/**
 * Фрагмент портфеля в [HostActivity][ru.surf.learn2invest.presentation.ui.components.screens.host.HostActivity]
 */
@AndroidEntryPoint
internal class PortfolioFragment : BaseResFragment() {
    private val viewModel: PortfolioFragmentViewModel by viewModels()


    @Inject
    lateinit var lineChartHelperFactory: LineChartHelper.Factory

    @Inject
    lateinit var adapterFactory: PortfolioAdapter.Factory

    // Создание представления фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        // Настройка цвета статус-бара
        activity?.apply {
            setStatusBarColor(
                window,
                this,
                R.color.accent_background,
                R.color.accent_background_dark
            )
        }

        return FragmentPortfolioBinding.inflate(inflater, container, false).also {
            initListeners(it)
        }.root
    }

    private fun initListeners(binding: FragmentPortfolioBinding) {
        // Настройка RecyclerView для отображения активов
        val adapter = adapterFactory.create { id, name, symbol ->
            viewModel.handleIntent(
                PortfolioFragmentIntent.StartAssetReviewActivity(
                    id = id,
                    name = name,
                    symbol = symbol
                )
            )
        }
        binding.assets.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.assets.adapter = adapter
        var chartHelper: LineChartHelper? = null

        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                if (chartHelper == null && state.dates.isNotEmpty()) {
                    chartHelper =
                        lineChartHelperFactory.create(AssetBalanceHistoryFormatter(state.dates))
                    chartHelper.setupChart(binding.chart)
                }
                chartHelper?.updateData(state.chartData)

                binding.balanceText.text = state.totalBalance.getWithCurrency()
                val isBalanceNonZero = state.totalBalance != 0f

                binding.chart.isVisible = isBalanceNonZero
                binding.percent.isVisible = isBalanceNonZero

                binding.accountFunds.text = state.fiatBalance.getWithCurrency()

                binding.assets.isVisible = state.assets.isNotEmpty()
                binding.assetsAreEmpty.isVisible = state.assets.isEmpty()
                adapter.assets = state.assets
                adapter.priceChanges = state.priceChanges

                binding.percent.apply {
                    val percentage = state.portfolioChangePercentage
                    // Форматирование и установка текста процента
                    text =
                        if (percentage == 0f) "%.2f%%".format(Locale.getDefault(), percentage)
                        else "%+.2f%%".format(Locale.getDefault(), percentage)

                    // Установка фона в зависимости от изменения
                    background = when {
                        percentage > 0 -> getDrawableRes(R.drawable.percent_increase_background)

                        percentage < 0 -> getDrawableRes(R.drawable.percent_recession_background)

                        else -> getDrawableRes(R.drawable.percent_zero_background)
                    }
                }
            }
        }

        lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is PortfolioFragmentEffect.StartAssetReviewActivity -> {
                        requireActivity().startActivity(
                            AssetReviewActivity.newIntent(
                                requireActivity() as AppCompatActivity,
                                effect.id,
                                effect.name,
                                effect.symbol
                            )
                        )
                    }

                    is PortfolioFragmentEffect.MailTo -> mailTo(effect.mail)
                    is PortfolioFragmentEffect.OpenLink -> openLink(effect.link)
                    PortfolioFragmentEffect.CloseDrawer -> closeDrawer(binding)
                    PortfolioFragmentEffect.OpenDrawer -> openDrawer(binding)
                    PortfolioFragmentEffect.OpenRefillAccountDialog ->
                        RefillAccountDialog().showDialog(parentFragmentManager)
                }
            }
        }

        // Кнопка пополнения счета
        binding.topUpBtn.setOnClickListener {
            viewModel.handleIntent(PortfolioFragmentIntent.OpenRefillAccountDialog)
        }


        // Инициализация слушателей для бокового меню
        initDrawerListeners(binding)

        // Открытие бокового меню по нажатию на кнопку
        binding.imageButton.setOnClickListener {
            viewModel.handleIntent(PortfolioFragmentIntent.OpenDrawer)
        }

        // Закрытие бокового меню при прикосновении к экрану
        binding.drawerLayout.setOnTouchListener { _, _ ->
            viewModel.handleIntent(PortfolioFragmentIntent.CloseDrawer)
            false
        }
    }

    // Метод для старта обновления цен при возобновлении видимости фрагмента
    override fun onStart() {
        super.onStart()
        viewModel.handleIntent(PortfolioFragmentIntent.StartRealtimeUpdate)
    }

    // Метод для остановки обновления цен при скрытии фрагмента
    override fun onStop() {
        super.onStop()
        viewModel.handleIntent(PortfolioFragmentIntent.StopRealtimeUpdate)
        viewModel.handleIntent(PortfolioFragmentIntent.CloseDrawer)
    }


    // Открытие бокового меню
    private fun openDrawer(binding: FragmentPortfolioBinding) {
        activity?.apply {
            val drawer = binding.drawerLayout

            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START)
            }
        }
    }

    // Закрытие бокового меню
    private fun closeDrawer(binding: FragmentPortfolioBinding) {
        activity?.apply {
            val drawer = binding.drawerLayout

            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
        }
    }

    // Инициализация слушателей для бокового меню
    private fun initDrawerListeners(binding: FragmentPortfolioBinding) {
        binding.apply {
            // Кнопка "Написать нам"
            contactUs.setOnClickListener {
                viewModel.handleIntent(PortfolioFragmentIntent.MailTo(DevStrLink.CHERY))
            }

            // Кнопка для перехода к исходному коду
            code.setOnClickListener {
                viewModel.handleIntent(PortfolioFragmentIntent.OpenLink(DevStrLink.CODE))
            }

            // Кнопка для перехода к макету в Figma
            figma.setOnClickListener {
                viewModel.handleIntent(PortfolioFragmentIntent.OpenLink(DevStrLink.FIGMA))
            }

            // Отображение версии приложения
            versionCode.text = requireContext().getVersionName()
        }
    }

    // Открытие внешней ссылки
    private fun openLink(link: String) = startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    private fun mailTo(mail: String) = startActivity(Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto: $mail".toUri()
    })
}
