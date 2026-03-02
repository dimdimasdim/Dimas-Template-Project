package com.dimas.dimasproject.feature.randomnumber.di

import com.dimas.dimasproject.feature.randomnumber.data.local.RandomNumberDao
import com.dimas.dimasproject.feature.randomnumber.data.repository.RandomNumberRepositoryImpl
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import com.dimas.dimasproject.core.local.db.AppDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RandomNumberModule {

    @Binds
    @Singleton
    abstract fun bindRandomNumberRepository(
        impl: RandomNumberRepositoryImpl
    ): RandomNumberRepository

    companion object {
        @Provides
        @Singleton
        fun provideRandomNumberDao(database: AppDatabase): RandomNumberDao =
            database.randomNumberDao()
    }
}

