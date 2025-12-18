package com.way.samurai.lessons

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object Lesson02ContextAndDispatchers {
    suspend fun cpuWorkload(): Int = withContext(Dispatchers.Default + CoroutineName("cpu")) {
        (1..1_000).sum()
    }

    suspend fun blockingIo(): String = withContext(Dispatchers.IO + CoroutineName("io")) {
        Thread.sleep(60)
        "Disk read complete"
    }

    suspend fun avoidDispatcherHops(): String = coroutineScope {
        val cachedResult = async { cpuWorkload() }
        val disk = async { blockingIo() }
        "cpu=${cachedResult.await()} | ${disk.await()}"
    }

    suspend fun demonstrateContextPreservation() = coroutineScope {
        launch(Dispatchers.Default + CoroutineName("fan-out")) {
            val result = avoidDispatcherHops()
            println("fan-out collected on ${kotlin.coroutines.coroutineContext[CoroutineName]}: $result")
        }
    }
}

@JvmName("Lesson02ContextMain")
fun main() = runBlocking {
    println("=== 02_context_and_dispatchers ===")
    println(Lesson02ContextAndDispatchers.avoidDispatcherHops())
    Lesson02ContextAndDispatchers.demonstrateContextPreservation()
}
