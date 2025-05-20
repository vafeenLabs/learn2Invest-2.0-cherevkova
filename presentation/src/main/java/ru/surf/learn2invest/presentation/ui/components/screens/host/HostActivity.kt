package ru.surf.learn2invest.presentation.ui.components.screens.host

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.ActivityHostBinding
import ru.surf.learn2invest.presentation.utils.setNavigationBarColor

/**
 * Главный экран приложения с BottomBar.
 * Эта активность управляет отображением основного контента и навигацией в приложении.
 * Она также настраивает нижнюю навигационную панель (BottomBar) и связывает ее с контроллером навигации.
 */
@AndroidEntryPoint
internal class HostActivity : AppCompatActivity() {

    /**
     * Метод жизненного цикла активити, вызываемый при создании.
     * Здесь настраивается внешний вид экрана и навигация.
     *
     * @param savedInstanceState Состояние, сохраненное при предыдущем запуске.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливает цвета для навигационной панели
        setNavigationBarColor(
            window, // окно активити
            this,   // контекст активности
            R.color.accent_background, // основной цвет фона
            R.color.accent_background_dark // цвет фона для темной темы
        )

        // Привязка интерфейса с использованием ActivityHostBinding
        val binding = ActivityHostBinding.inflate(layoutInflater)

        // Устанавливаем разметку для активности
        setContentView(binding.root)

        // Находим контейнер для фрагментов и получаем контроллер навигации
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.host_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        // Настройка нижней навигационной панели (Bottom Navigation) для работы с контроллером навигации
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val navController = navHostFragment.navController
            val currentDestinationId = navController.currentDestination?.id

            if (currentDestinationId == menuItem.itemId) {
                // Уже на этом экране — не навигируем
                false
            } else {
                // Навигация с popUpTo, чтобы не создавать дубликаты в back stack
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true) // Если уже вверху стека — не создаём новый экземпляр
                    .setRestoreState(true)    // Восстанавливаем состояние, если фрагмент уже в back stack
                    .setPopUpTo(
                        menuItem.itemId,
                        false
                    ) // Убираем все фрагменты выше выбранного, но не удаляем сам
                    .build()

                navController.navigate(menuItem.itemId, null, navOptions)
                true
            }
        }
    }
}
