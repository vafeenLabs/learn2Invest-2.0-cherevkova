package ru.surf.learn2invest.presentation.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.ActivityMainBinding
import ru.surf.learn2invest.presentation.utils.setNavigationBarColor
import ru.surf.learn2invest.presentation.utils.setStatusBarColor

@AndroidEntryPoint
internal class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor(window, this, R.color.black, R.color.black)
        setNavigationBarColor(window, this, R.color.black, R.color.black)

        initListeners()
        viewModel.handleIntent(MainActivityIntent.ProcessSplash(binding.splashTextView))
    }

    private fun initListeners() {
        lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    MainActivityEffect.Finish -> this@MainActivity.finish()
                    is MainActivityEffect.StartIntent -> startActivity(effect.creating(this@MainActivity))
                }
            }
        }
    }

}
