package com.way.coroutines.examples.structured

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking(CoroutineName("04-structured")) {
    val logger = Logger("04-structured")
    parentChildCancellation(logger)
    supervisorIsolation(logger)
}

private suspend fun parentChildCancellation(logger: Logger) = coroutineScope {
    val parent = launch(CoroutineName("parent")) {
        val child = launch(CoroutineName("child")) {
            delay(200)
            error("child failed")
        }

        try {
            child.join()
        } finally {
            logger.warn("Parent cancelled after child failure: ${this.coroutineContext[CoroutineName]}")
        }
    }
    parent.join()
}

private suspend fun supervisorIsolation(logger: Logger) = kotlinx.coroutines.supervisorScope {
    val fast = async(CoroutineName("fast")) {
        delay(100)
        "fast-value"
    }
    val failing = async(CoroutineName("failing")) {
        delay(150)
        error("boom")
    }

    val slow = async(CoroutineName("slow")) {
        delay(200)
        "slow-value"
    }

    kotlin.runCatching { failing.await() }
    logger.info("Supervisor kept siblings alive: ${fast.await()} & ${slow.await()}")
}
