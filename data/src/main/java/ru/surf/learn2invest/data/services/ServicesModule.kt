package ru.surf.learn2invest.data.services

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.surf.learn2invest.data.services.coin_icon_loader.CoinIconLoaderImpl
import ru.surf.learn2invest.data.services.settings_manager.SettingsManagerImpl
import ru.surf.learn2invest.data.services.settings_manager.SharedPreferencesValue
import ru.surf.learn2invest.domain.services.settings_manager.SettingsManager
import ru.surf.learn2invest.domain.services.coin_icon_loader.CoinIconLoader
import javax.inject.Singleton

@Module
@InstallIn(
    ViewModelComponent::class,
    FragmentComponent::class,
    ActivityRetainedComponent::class
)
internal class ServicesModule {
    @Provides
    fun provideCoinIconLoaderImpl(impl: CoinIconLoaderImpl): CoinIconLoader = impl
}

@Module
@InstallIn(SingletonComponent::class)
internal class SingletonServices {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context
            .getSharedPreferences(SharedPreferencesValue.NAME, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideSettingsManager(impl: SettingsManagerImpl): SettingsManager = impl
}