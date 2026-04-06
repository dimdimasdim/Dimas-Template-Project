package com.dimas.dimasproject.di

import androidx.room.Room
import com.dimas.dimasproject.core.local.db.AppDatabase
import com.dimas.dimasproject.core.local.preferences.AppSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    single { AppSettings(get()) }
}
