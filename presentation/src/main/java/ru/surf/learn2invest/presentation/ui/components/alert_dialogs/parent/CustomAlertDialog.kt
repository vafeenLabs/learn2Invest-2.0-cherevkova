package ru.surf.learn2invest.presentation.ui.components.alert_dialogs.parent


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ru.surf.learn2invest.presentation.databinding.SimpleDialogBinding

/**
 * Абстрактный класс для удобного создания AlertDialog с привязкой к разметке.
 * Все диалоги, наследующие этот класс, должны реализовывать `initListeners()`
 * для установки слушателей событий.
 */
internal abstract class CustomAlertDialog : DialogFragment() {

    /**
     * Уникальный тег для идентификации диалогового окна в FragmentManager.
     */
    abstract val dialogTag: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = SimpleDialogBinding.inflate(inflater)
        initListeners(binding)
        return binding.root
    }

    /**
     * Метод для инициализации слушателей событий внутри диалогового окна.
     * Должен быть реализован в наследуемых классах.
     */
    abstract fun initListeners(binding: SimpleDialogBinding)

    /**
     * Показывает диалоговое окно, используя переданный FragmentManager.
     *
     * @param fragmentManager Менеджер фрагментов, через который будет показан диалог.
     */
    fun showDialog(fragmentManager: FragmentManager) = show(fragmentManager, this.dialogTag)
}
