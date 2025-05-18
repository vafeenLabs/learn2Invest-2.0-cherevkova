package ru.surf.learn2invest.presentation.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

internal sealed class MainActivityEffect {
    data object Finish : MainActivityEffect()
    data class StartIntent(val creating: (AppCompatActivity) -> Intent) : MainActivityEffect()
}