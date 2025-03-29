package ru.surf.learn2invest.domain.animator

import android.animation.Animator
import android.view.View

/**
 * Интерфейс для кастомной анимации.
 */
interface CustomAnimator {

    /**
     * Анимирует прозрачность представления.
     *
     * @param view        Представление, которое будет анимировано.
     * @param duration    Продолжительность анимации в миллисекундах.
     * @param onStart     Callback, вызываемый при начале анимации. По умолчанию — null.
     * @param onEnd       Callback, вызываемый при завершении анимации. По умолчанию — null.
     * @param onCancel    Callback, вызываемый при отмене анимации. По умолчанию — null.
     * @param onRepeat    Callback, вызываемый при повторении анимации. По умолчанию — null.
     * @param values      Массив значений прозрачности, через которые будет проходить анимация.
     */
    fun animateViewAlpha(
        view: View,
        duration: Long,
        onStart: (() -> Unit)? = null,
        onEnd: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onRepeat: (() -> Unit)? = null,
        vararg values: Float,
    )

    /**
     * Анимирует горизонтальный байас представления.
     *
     * @param view        Представление, которое будет анимировано.
     * @param duration    Продолжительность анимации в миллисекундах.
     * @param onStart     Callback, вызываемый при начале анимации. По умолчанию — null.
     * @param onEnd       Callback, вызываемый при завершении анимации. По умолчанию — null.
     * @param onCancel    Callback, вызываемый при отмене анимации. По умолчанию — null.
     * @param onRepeat    Callback, вызываемый при повторении анимации. По умолчанию — null.
     * @param values      Массив значений байаса, через которые будет проходить анимация.
     * @return Объект аниматора.
     */
    fun animateHorizontalBias(
        view: View,
        duration: Long,
        onStart: (() -> Unit)? = null,
        onEnd: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onRepeat: (() -> Unit)? = null,
        vararg values: Float,
    ): Animator
}
