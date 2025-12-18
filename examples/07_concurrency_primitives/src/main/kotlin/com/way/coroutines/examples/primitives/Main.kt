package com.way.coroutines.examples.primitives

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

fun main() = runBlocking(CoroutineName("07-primitives")) {
    val logger = Logger("07-primitives")
    mutexExample(logger)
    semaphoreExample(logger)
    rateLimitExample(logger)
    atomicExample(logger)
}

private suspend fun mutexExample(logger: Logger) {
    val mutex = Mutex()
    var balance = 0
    coroutineScope {
        val jobs = List(10) {
            launch {
                repeat(100) {
                    mutex.withLock { balance++ }
                }
            }
        }
        jobs.forEach { it.join() }
    }
    logger.info("Final balance with Mutex = $balance")
}

private suspend fun semaphoreExample(logger: Logger) = coroutineScope {
    val semaphore = Semaphore(permits = 3)
    val work = List(10) { index ->
        async(CoroutineName("worker-$index")) {
            semaphore.acquire()
            try {
                logger.info("Running limited task $index")
                delay(50)
            } finally {
                semaphore.release()
            }
        }
    }
    work.awaitAll()
}

private suspend fun rateLimitExample(logger: Logger) {
    var lastTimestamp = 0L
    repeat(5) { index ->
        val now = System.currentTimeMillis()
        val wait = max(0, lastTimestamp + 100 - now)
        if (wait > 0) delay(wait)
        lastTimestamp = System.currentTimeMillis()
        logger.info("Rate-limited request $index at ${lastTimestamp % 10_000}")
    }
}

private fun atomicExample(logger: Logger) {
    val atomicCounter = AtomicInteger(0)
    val threads = List(4) {
        Thread {
            repeat(1_000) { atomicCounter.incrementAndGet() }
        }
    }
    threads.forEach(Thread::start)
    threads.forEach(Thread::join)
    logger.info("Atomic counter value = ${atomicCounter.get()}")
}
