package ru.surf.learn2invest.data.database_components.di_module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import ru.surf.learn2invest.data.database_components.L2IDatabase
import ru.surf.learn2invest.data.database_components.dao.AssetBalanceHistoryDao
import ru.surf.learn2invest.data.database_components.dao.AssetInvestDao
import ru.surf.learn2invest.data.database_components.dao.SearchedCoinDao
import ru.surf.learn2invest.data.database_components.dao.TransactionDao

@Module
@InstallIn(ActivityRetainedComponent::class)
class DaoDiModule {

    @Provides
    internal fun assetBalanceHistoryDao(db: L2IDatabase): AssetBalanceHistoryDao =
        db.assetBalanceHistoryDao()

    @Provides
    internal fun assetInvestDao(db: L2IDatabase): AssetInvestDao = db.assetInvestDao()


    @Provides
    internal fun searchedCoinDao(db: L2IDatabase): SearchedCoinDao = db.searchedCoinDao()

    @Provides
    internal fun transactionDao(db: L2IDatabase): TransactionDao = db.transactionDao()

}