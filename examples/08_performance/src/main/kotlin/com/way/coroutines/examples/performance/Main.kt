package com.way.coroutines.examples.performance

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

fun main() = runBlocking(CoroutineName("08-performance")) {
    val logger = Logger("08-performance")
    avoidContextThrash(logger)
    yieldForCooperation(logger)
}

private suspend fun avoidContextThrash(logger: Logger) {
    val elapsed = measureTimeMillis {
        val results = listOf(
            async(Dispatchers.Default) { cpuBound(2000) },
            async(Dispatchers.Default) { cpuBound(2500) },
        ).awaitAll()
        logger.info("Computed cpu tasks: ${results.joinToString()}")
    }
    logger.info("Computed without redundant withContext in ${elapsed}ms")
}

private suspend fun yieldForCooperation(logger: Logger) {
    val job = launch(CoroutineName("cooperative-heavy")) {
        var acc = 0L
        repeat(5) { chunk ->
            repeat(500_000) { acc += it }
            kotlinx.coroutines.yield()
            logger.info("Processed chunk $chunk on ${Thread.currentThread().name}")
        }
        logger.info("Final acc = $acc")
    }
    delay(50)
    job.join()
}

private suspend fun cpuBound(work: Int): Long = withContext(Dispatchers.Default) {
    var acc = 0L
    repeat(work) { acc += it }
    acc
}
