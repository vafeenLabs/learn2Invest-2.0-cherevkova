package ru.surf.learn2invest.data.database_components.di_module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.surf.learn2invest.data.converters.ProfileConverter
import ru.surf.learn2invest.data.database_components.L2IDatabase
import ru.surf.learn2invest.data.database_components.dao.ProfileDao
import ru.surf.learn2invest.data.database_components.repository.ProfileRepositoryImpl
import ru.surf.learn2invest.domain.database.repository.ProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseDIModule {
    @Provides
    @Singleton
    internal fun injectDatabase(@ApplicationContext context: Context): L2IDatabase =
        Room.databaseBuilder(
            context = context, klass = L2IDatabase::class.java, name = L2IDatabase.Companion.NAME
        ).build()
    @Provides
    internal fun provideProfileRepository(
        dao: ProfileDao,
        converter: ProfileConverter
    ): ProfileRepository = ProfileRepositoryImpl(dao, converter)

    @Provides
    internal fun profileDao(db: L2IDatabase): ProfileDao = db.profileDao()

}