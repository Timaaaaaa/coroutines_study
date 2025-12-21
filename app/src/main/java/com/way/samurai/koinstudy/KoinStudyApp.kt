package com.way.samurai.koinstudy

import android.app.Application
import com.way.samurai.koinstudy.di.coreModule
import com.way.samurai.koinstudy.di.dataModule
import com.way.samurai.koinstudy.di.featureTasksModule
import com.way.samurai.koinstudy.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.koin.startKoin
import org.koin.core.logger.Level

class KoinStudyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KoinStudyApp)
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
            modules(
                listOf(
                    coreModule,
                    networkModule,
                    dataModule,
                    featureTasksModule,
                )
            )
        }
    }
}
