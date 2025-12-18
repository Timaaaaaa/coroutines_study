package com.way.samurai.lessons

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

object Lesson08Performance {
    suspend fun cpuHeavyCalculation(): Int = withContext(Dispatchers.Default) {
        var total = 0
        repeat(5_000) {
            total += it
            if (it % 500 == 0) yield()
        }
        total
    }

    suspend fun ioBoundBatch(urls: List<String>): List<String> = coroutineScope {
        urls.map { url ->
            async(Dispatchers.IO) {
                delay(20)
                println("Fetched $url without excessive context switches")
                url
            }
        }.map { it.await() }
    }
}

object Lesson08PerformanceLauncher {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("=== 08_performance ===")
        println("CPU heavy result: ${Lesson08Performance.cpuHeavyCalculation()}")
        Lesson08Performance.ioBoundBatch(listOf("/profile", "/feed", "/messages"))
    }
}
