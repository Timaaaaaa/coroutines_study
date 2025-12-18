package com.way.coroutines.examples.testing

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking(CoroutineName("09-testing")) {
    val logger = Logger("09-testing")
    logger.info("Demonstrating runTest-like control with virtual time is available in tests.")
    val job = launch {
        repeat(3) { index ->
            delay(50)
            logger.info("real delay tick $index")
        }
    }
    job.join()
}
