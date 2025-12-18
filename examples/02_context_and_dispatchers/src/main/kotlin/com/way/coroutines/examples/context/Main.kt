package com.way.coroutines.examples.context

import com.way.coroutines.core.DispatchersProvider
import com.way.coroutines.core.Logger
import com.way.coroutines.core.StandardDispatchers
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking(CoroutineName("02-context")) {
    val logger = Logger("02-context")
    demonstrateContexts(StandardDispatchers, logger)
}

suspend fun demonstrateContexts(dispatchers: DispatchersProvider, logger: Logger) = coroutineScope {
    val ioWork = async(dispatchers.io + CoroutineName("io-call")) {
        logger.info("IO dispatcher on ${Thread.currentThread().name}")
        "io-result"
    }

    val cpuWork = async(dispatchers.default + CoroutineName("cpu-call")) {
        logger.info("Default dispatcher on ${Thread.currentThread().name}")
        heavyCpuWork()
    }

    val child: Job = launch(CoroutineName("child-with-context")) {
        logger.info("Child inherits parent dispatcher: ${Thread.currentThread().name}")
        val refined = withContext(dispatchers.default) {
            logger.info("Switch only when needed: ${Thread.currentThread().name}")
            cpuWork.await()
        }
        logger.info("Child result = ${refined} + ${ioWork.await()}")
    }
    child.join()
}

private fun heavyCpuWork(): String {
    // Avoid dispatcher switches in tight loops; just return quickly here.
    var acc = 0L
    repeat(1_000) { acc += it }
    return "cpu-$acc"
}
