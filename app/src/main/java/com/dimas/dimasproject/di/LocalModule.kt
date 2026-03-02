package com.dimas.dimasproject.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.dimas.dimasproject.core.local.db.AppDatabase
import com.dimas.dimasproject.core.local.preferences.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideAppSettings(
        dataStore: DataStore<Preferences>
    ): AppSettings = AppSettings(dataStore)
}

