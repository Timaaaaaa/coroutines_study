package com.way.samurai.koinstudy

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.way.samurai.koinstudy.di.coreModule
import com.way.samurai.koinstudy.di.dataModule
import com.way.samurai.koinstudy.di.featureTasksModule
import com.way.samurai.koinstudy.di.networkModule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class KoinInstrumentationSmokeTest {

    @Test
    fun start_and_stop_koin() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(
                coreModule,
                networkModule,
                dataModule,
                featureTasksModule,
            )
        }.also { stopKoin() }
    }
}
