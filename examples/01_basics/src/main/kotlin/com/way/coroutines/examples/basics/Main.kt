package com.way.coroutines.examples.basics

import com.way.coroutines.core.Logger
import com.way.coroutines.core.StandardDispatchers
import com.way.coroutines.core.data.FakeApi
import com.way.coroutines.core.data.Repository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val logger = Logger("01-basics")
    val repo = Repository(FakeApi(), com.way.coroutines.core.data.FakeDb(), StandardDispatchers, logger)

    logger.info("Starting basic coroutine examples on ${Thread.currentThread().name}")
    coroutineScope {
        val tasks = listOf(
            async { expensiveComputation("alpha") },
            async { expensiveComputation("beta") },
            async { expensiveComputation("gamma") },
        )
        val results = tasks.awaitAll()
        logger.info("Fan-in result: ${results.joinToString()}")
    }

    logger.info("Fetching user and config with structured async")
    repo.refreshConfigAndUser(id = "primary").also { logger.info("Result: $it") }
}

private suspend fun expensiveComputation(label: String): String {
    delay(120)
    return "$label-done"
}
