package com.dimas.dimasproject

import android.app.Application
import com.dimas.dimasproject.di.coreModule
import com.dimas.dimasproject.di.localModule
import com.dimas.dimasproject.di.networkModule
import com.dimas.dimasproject.feature.randomnumber.di.randomNumberModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DimasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@DimasApp)
            modules(
                coreModule,
                networkModule,
                localModule,
                randomNumberModule
            )
        }
    }
}
