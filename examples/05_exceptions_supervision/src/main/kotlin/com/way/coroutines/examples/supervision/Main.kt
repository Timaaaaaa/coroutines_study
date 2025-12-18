package com.way.coroutines.examples.supervision

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking(CoroutineName("05-supervision")) {
    val logger = Logger("05-supervision")
    val handler = CoroutineExceptionHandler { context, throwable ->
        logger.warn("Caught in handler from ${context[CoroutineName]}: ${throwable.message}")
    }

    val supervisorScope = kotlinx.coroutines.CoroutineScope(coroutineContext + SupervisorJob() + handler)
    supervisorScope.launch(CoroutineName("failing-child")) {
        throw IllegalStateException("top-level failure")
    }

    val success = supervisorScope.async(CoroutineName("survivor")) {
        "still-running"
    }

    logger.info("Supervisor result survives failure: ${success.await()}")
}
