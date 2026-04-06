package com.dimas.dimasproject.feature.randomnumber.di

import com.dimas.dimasproject.core.local.db.AppDatabase
import com.dimas.dimasproject.feature.randomnumber.data.local.RandomNumberLocalDataSource
import com.dimas.dimasproject.feature.randomnumber.data.remote.RandomNumberRemoteDataSource
import com.dimas.dimasproject.feature.randomnumber.data.repository.RandomNumberRepositoryImpl
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.FetchRandomNumberUseCase
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.GetAllRandomNumbersUseCase
import com.dimas.dimasproject.feature.randomnumber.presentation.RandomNumberViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val randomNumberModule = module {
    single { get<AppDatabase>().randomNumberDao() }
    single { RandomNumberRemoteDataSource(get()) }
    single { RandomNumberLocalDataSource(get()) }
    single<RandomNumberRepository> { RandomNumberRepositoryImpl(get(), get()) }
    factory { FetchRandomNumberUseCase(get()) }
    factory { GetAllRandomNumbersUseCase(get()) }
    viewModel { RandomNumberViewModel(get(), get()) }
}
