package ru.surf.learn2invest.presentation.ui.main

import android.widget.TextView

internal sealed class MainActivityIntent {
    data class ProcessSplash(val textView: TextView) : MainActivityIntent()
//    data class AnimateGreeting(
//        val textView: TextView,
//        val duration: Long,
//        val onStart: (() -> Unit)? = null,
//        val onEnd: (() -> Unit)? = null,
//        val onCancel: (() -> Unit)? = null,
//        val onRepeat: (() -> Unit)? = null,
//        val values: List<Float>,
//    ): MainActivityIntent()
}