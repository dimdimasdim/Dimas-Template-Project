package com.dimas.dimasproject.di

import com.dimas.dimasproject.core.network.NetworkClient
import org.koin.dsl.module

val networkModule = module {
    single { NetworkClient.create() }
}
