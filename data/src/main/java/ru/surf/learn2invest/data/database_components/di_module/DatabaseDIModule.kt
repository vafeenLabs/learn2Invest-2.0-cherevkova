package ru.surf.learn2invest.data.database_components.di_module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.surf.learn2invest.data.database_components.L2IDatabase
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
}