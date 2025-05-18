package ru.surf.learn2invest.presentation.ui.components.screens.fragments.marketreview

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchIO
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.FragmentMarketReviewBinding
import ru.surf.learn2invest.presentation.ui.components.screens.fragments.common.BaseResFragment
import ru.surf.learn2invest.presentation.utils.setStatusBarColor
import ru.surf.learn2invest.presentation.utils.textListener
import javax.inject.Inject

/**
 * Фрагмент обзора рынка в [HostActivity][ru.surf.learn2invest.ui.components.screens.host.HostActivity]
 */
@AndroidEntryPoint
internal class MarketReviewFragment : BaseResFragment() {
    private val binding by lazy { FragmentMarketReviewBinding.inflate(layoutInflater) }
    private val viewModel: MarketReviewFragmentViewModel by viewModels()

    private var realTimeUpdateJob: Job? = null

    @Inject
    lateinit var adapterFactory: MarketReviewAdapter.Factory

    private val adapter: MarketReviewAdapter by lazy {
        adapterFactory.create {
            viewModel.handleIntent(MarketReviewFragmentIntent.AddSearchedCoin(it))
        }
    }

    // Создание представления фрагмента
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Установка цвета для статус-бара
        activity?.apply {
            setStatusBarColor(window, this, R.color.white, R.color.main_background_dark)
        }
        initListeners(binding)
        return binding.root
    }

    private fun initListeners(binding: FragmentMarketReviewBinding) {
        // Настройка RecyclerView для отображения данных
        binding.marketReviewRecyclerview.layoutManager = LinearLayoutManager(this.requireContext())
        binding.marketReviewRecyclerview.adapter = adapter
        // Слушаем изменения фильтрации
        viewLifecycleOwner.lifecycleScope.launchMAIN {
            viewModel.state.collectLatest { state ->
                binding.apply {
                    if (state.filterOrder) {
                        filterByPrice.setIconResource(R.drawable.arrow_top_green)
                        filterByPrice.setIconTintResource(R.color.label)
                    } else {
                        filterByPrice.setIconResource(R.drawable.arrow_bottom_red)
                        filterByPrice.setIconTintResource(R.color.recession)
                    }
                    marketReviewRecyclerview.isVisible = !state.isLoading
                    binding.progressBar.isVisible = state.isLoading
                    marketReviewRecyclerview.isVisible = !state.isError
                    networkErrorTv.isVisible = state.isError
                    networkErrorIv.isVisible = state.isError

                    if (!state.isSearch) {
                        searchEditText.text.clear()
                    }
                    youSearch.isVisible =
                        state.data.isNotEmpty() && state.searchRequest.isBlank() && state.isSearch
                    clearTv.isVisible =
                        state.data.isNotEmpty() && state.searchRequest.isBlank() && state.isSearch
                    cancelTV.isVisible = state.isSearch
                    filterByPrice.isVisible = !state.isSearch
                    filterByMarketcap.isVisible = !state.isSearch
                    filterByChangePercent24Hr.isVisible = !state.isSearch
                    adapter.data = when {
                        state.isSearch && state.searchRequest.isNotEmpty() -> state.dataBySearchRequest
                        state.isSearch && state.searchRequest.isEmpty() -> state.searchedData
                        else -> state.data
                    }
                    state.filterState.also {
                        val isDarkTheme =
                            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

                        filterByMarketcap.backgroundTintList =
                            ColorStateList.valueOf(
                                getColorRes(
                                    if (it == FilterState.FILTER_BY_MARKETCAP) {
                                        if (isDarkTheme)
                                            R.color.accent_background_dark
                                        else
                                            R.color.accent_background
                                    } else {
                                        if (isDarkTheme)
                                            R.color.accent_button_dark
                                        else
                                            R.color.view_background
                                    }
                                )
                            )
                        filterByChangePercent24Hr.backgroundTintList =
                            ColorStateList.valueOf(
                                getColorRes(
                                    if (it == FilterState.FILTER_BY_PERCENT) {
                                        if (isDarkTheme)
                                            R.color.accent_background_dark
                                        else
                                            R.color.accent_background
                                    } else {
                                        if (isDarkTheme)
                                            R.color.accent_button_dark
                                        else
                                            R.color.view_background
                                    }
                                )
                            )
                        filterByPrice.backgroundTintList =
                            ColorStateList.valueOf(
                                getColorRes(
                                    if (it == FilterState.FILTER_BY_PRICE) {
                                        if (isDarkTheme)
                                            R.color.accent_background_dark
                                        else
                                            R.color.accent_background
                                    } else {
                                        if (isDarkTheme)
                                            R.color.accent_button_dark
                                        else
                                            R.color.view_background
                                    }
                                )
                            )
                    }

                }
            }
        }
        // Настройка обработчиков нажатий на кнопки фильтрации
        binding.apply {
            filterByMarketcap.setOnClickListener {
                marketReviewRecyclerview.layoutManager?.scrollToPosition(0)
                viewModel.handleIntent(MarketReviewFragmentIntent.FilterByMarketCap)
            }

            filterByChangePercent24Hr.setOnClickListener {
                marketReviewRecyclerview.layoutManager?.scrollToPosition(0)
                viewModel.handleIntent(MarketReviewFragmentIntent.FilterByPercent)
            }

            filterByPrice.setOnClickListener {
                marketReviewRecyclerview.layoutManager?.scrollToPosition(0)
                viewModel.handleIntent(MarketReviewFragmentIntent.FilterByPrice)
            }

            searchEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) viewModel.handleIntent(MarketReviewFragmentIntent.SetSearchState(true))
            }


            clearTv.setOnClickListener {
                viewModel.handleIntent(MarketReviewFragmentIntent.ClearSearchData)
            }

            cancelTV.setOnClickListener {
                viewModel.handleIntent(MarketReviewFragmentIntent.SetSearchState(false))
                hideKeyboardFrom(requireContext(), searchEditText)
            }
            searchEditText.addTextChangedListener(textListener(afterTextChanged = {
                viewModel.handleIntent(
                    MarketReviewFragmentIntent.UpdateSearchRequest(searchRequest = it.toString())
                )
            }))
        }
    }

    // Запуск обновлений данных в реальном времени
    override fun onResume() {
        super.onResume()
        realTimeUpdateJob = startRealtimeUpdate()
    }

    // Остановка обновлений данных при приостановке фрагмента
    override fun onPause() {
        super.onPause()
        realTimeUpdateJob?.cancel()
    }

    // Функция для обновления данных каждую 5 секунд
    private fun startRealtimeUpdate() = lifecycleScope.launchIO {
        while (true) {
            delay(5000)
            val firstElement =
                (binding.marketReviewRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val lastElement =
                (binding.marketReviewRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
//            viewModel.handleIntent(MarketReviewFragmentIntent.UpdateData(firstElement, lastElement))
        }
    }

    // Функция для скрытия клавиатуры
    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}
