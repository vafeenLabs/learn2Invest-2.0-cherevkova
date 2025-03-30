package ru.surf.learn2invest.data.database_components.di_module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import ru.surf.learn2invest.data.converters.AssetBalanceHistoryConverter
import ru.surf.learn2invest.data.converters.AssetInvestConverter
import ru.surf.learn2invest.data.converters.SearchedCoinConverter
import ru.surf.learn2invest.data.converters.TransactionConverter
import ru.surf.learn2invest.data.database_components.dao.AssetBalanceHistoryDao
import ru.surf.learn2invest.data.database_components.dao.AssetInvestDao
import ru.surf.learn2invest.data.database_components.dao.SearchedCoinDao
import ru.surf.learn2invest.data.database_components.dao.TransactionDao
import ru.surf.learn2invest.data.database_components.repository.AppDatabaseRepositoryImpl
import ru.surf.learn2invest.data.database_components.repository.AssetBalanceHistoryRepositoryImpl
import ru.surf.learn2invest.data.database_components.repository.AssetInvestRepositoryImpl
import ru.surf.learn2invest.data.database_components.repository.SearchedCoinRepositoryImpl
import ru.surf.learn2invest.data.database_components.repository.TransactionRepositoryImpl
import ru.surf.learn2invest.domain.database.repository.AppDatabaseRepository
import ru.surf.learn2invest.domain.database.repository.AssetBalanceHistoryRepository
import ru.surf.learn2invest.domain.database.repository.AssetInvestRepository
import ru.surf.learn2invest.domain.database.repository.SearchedCoinRepository
import ru.surf.learn2invest.domain.database.repository.TransactionRepository

@Module
@InstallIn(ActivityRetainedComponent::class, ViewModelComponent::class)
class RepositoryDiModule {
    @Provides
    internal fun provideAssetBalanceHistoryRepository(
        dao: AssetBalanceHistoryDao,
        converter: AssetBalanceHistoryConverter
    ): AssetBalanceHistoryRepository = AssetBalanceHistoryRepositoryImpl(dao, converter)

    @Provides
    internal fun provideAssetInvestRepository(
        dao: AssetInvestDao,
        converter: AssetInvestConverter
    ): AssetInvestRepository = AssetInvestRepositoryImpl(dao, converter)


    @Provides
    internal fun provideSearchedCoinRepository(
        dao: SearchedCoinDao,
        converter: SearchedCoinConverter
    ): SearchedCoinRepository = SearchedCoinRepositoryImpl(dao, converter)

    @Provides
    internal fun provideTransactionRepository(
        dao: TransactionDao,
        converter: TransactionConverter
    ): TransactionRepository = TransactionRepositoryImpl(dao, converter)

    @Provides
    internal fun provideAppDatabaseRepository(impl: AppDatabaseRepositoryImpl): AppDatabaseRepository =
        impl
}