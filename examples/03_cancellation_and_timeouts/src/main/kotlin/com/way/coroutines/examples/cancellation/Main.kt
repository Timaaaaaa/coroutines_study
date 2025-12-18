package com.way.coroutines.examples.cancellation

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

fun main() = runBlocking(CoroutineName("03-cancel")) {
    val logger = Logger("03-cancel")
    logger.info("Timeout demo result: ${timeoutDemo(logger)}")
    cooperativeCancellation(logger)
}

suspend fun timeoutDemo(logger: Logger): String? = withTimeoutOrNull(300) {
    repeat(5) { index ->
        kotlinx.coroutines.delay(100)
        logger.info("tick $index")
    }
    "completed"
}

suspend fun cooperativeCancellation(logger: Logger) {
    val job = launch(CoroutineName("cooperative")) {
        try {
            repeat(10) { index ->
                ensureActive()
                kotlinx.coroutines.delay(80)
                logger.info("working $index")
            }
        } finally {
            withContext(NonCancellable) {
                logger.info("cleanup in finally without being cancelled")
            }
        }
    }

    kotlinx.coroutines.delay(210)
    logger.warn("Cancelling job cooperatively")
    job.cancel(CancellationException("user dismissed"))
    job.join()
}
