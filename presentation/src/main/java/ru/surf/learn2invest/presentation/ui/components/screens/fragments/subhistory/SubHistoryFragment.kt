package ru.surf.learn2invest.presentation.ui.components.screens.fragments.subhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.databinding.FragmentAssetHistoryBinding
import ru.surf.learn2invest.presentation.utils.NoArgException
import ru.surf.learn2invest.presentation.utils.viewModelCreator
import javax.inject.Inject

/**
 * Фрагмент для отображения истории сделок с конкретной монетой в активе.
 */
@AndroidEntryPoint
internal class SubHistoryFragment : Fragment() {
    @Inject
    lateinit var factory: SubHistoryFragmentViewModel.Factory

    @Inject
    lateinit var adapter: SubHistoryAdapter

    // Создание ViewModel с передачей символа монеты для фильтрации данных
    private val viewModel: SubHistoryFragmentViewModel by viewModelCreator {
        factory.createSubHistoryAssetViewModel(
            symbol = requireArguments().getString(SYMBOL_KEY) ?: throw NoArgException(SYMBOL_KEY)
        )
    }

    /**
     * Инфлейт и настройка layout для фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAssetHistoryBinding.inflate(inflater, container, false)
        initListeners(binding)
        return binding.root
    }

    private fun initListeners(binding: FragmentAssetHistoryBinding) {
        binding.assetHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.assetHistory.adapter = adapter
        lifecycleScope.launchMAIN {
            viewModel.state.collectLatest {
                adapter.data = it.data
                // Отображение данных или сообщение об отсутствии сделок
                binding.apply {
                    assetHistory.isVisible = it.data.isNotEmpty()
                    noActionsError.isVisible = it.data.isEmpty()
                }
            }
        }
    }

    /**
     * Создание экземпляра фрагмента с передачей символа монеты.
     */
    companion object {
        private const val SYMBOL_KEY = "SYMBOL_KEY"
        fun newInstance(symbol: String): SubHistoryFragment = SubHistoryFragment().also {
            it.arguments = Bundle().apply {
                putString(SYMBOL_KEY, symbol)
            }
        }
    }
}
