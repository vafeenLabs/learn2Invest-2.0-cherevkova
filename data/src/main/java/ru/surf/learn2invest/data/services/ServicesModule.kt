package ru.surf.learn2invest.data.services

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import ru.surf.learn2invest.data.services.coin_icon_loader.CoinIconLoaderImpl
import ru.surf.learn2invest.domain.services.coin_icon_loader.CoinIconLoader

@Module
@InstallIn(ViewModelComponent::class, FragmentComponent::class)
internal class ServicesModule {
    @Provides
    fun provideCoinIconLoaderImpl(impl: CoinIconLoaderImpl): CoinIconLoader = impl
}

