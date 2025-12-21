package com.way.samurai.koinstudy

import com.way.samurai.koinstudy.di.coreModule
import com.way.samurai.koinstudy.di.dataModule
import com.way.samurai.koinstudy.di.featureTasksModule
import com.way.samurai.koinstudy.di.networkModule
import org.junit.Test
import org.koin.test.AutoCloseKoinTest
import org.koin.test.check.checkModules

class KoinGraphCheckTest : AutoCloseKoinTest() {
    @Test
    fun `graph is valid`() {
        checkModules {
            modules(
                coreModule,
                networkModule,
                dataModule,
                featureTasksModule,
            )
        }
    }
}
