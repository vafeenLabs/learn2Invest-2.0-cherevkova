package ru.surf.learn2invest.data.network_components

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.surf.learn2invest.data.services.coin_api_service.CoinAPIService
import ru.surf.learn2invest.data.services.coin_api_service.impl.CoinApiServiceImpl
import ru.surf.learn2invest.domain.network.NetworkRepository


@Module
@InstallIn(ActivityRetainedComponent::class)
internal object NetworkDIModule {

    @Provides
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context = context).components {
            add(SvgDecoder.Factory())
        }.build()

    @Provides
    fun provideNetworkRepository(networkRepositoryImpl: NetworkRepositoryImpl): NetworkRepository =
        networkRepositoryImpl

    @Provides
    fun provideCoinAPIService(impl: CoinApiServiceImpl): CoinAPIService = impl
//    Retrofit.Builder().baseUrl(RetrofitLinks.BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .client(
//            OkHttpClient.Builder()
//                .addInterceptor(
//                    HttpLoggingInterceptor()
//                        .setLevel(HttpLoggingInterceptor.Level.BASIC)
//                )
//                .build()
//        )
//        .build()
//        .create(CoinAPIService::class.java)
}