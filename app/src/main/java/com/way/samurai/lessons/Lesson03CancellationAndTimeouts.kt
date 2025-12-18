package com.way.samurai.lessons

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

object Lesson03CancellationAndTimeouts {
    suspend fun cancellablePipeline(): String = coroutineScope {
        val job = launch {
            try {
                repeat(5) { step ->
                    ensureActive()
                    delay(50)
                    println("working step $step")
                }
            } finally {
                withContext(NonCancellable) {
                    println("Cleaning up resources even when cancelled")
                }
            }
        }
        delay(120)
        job.cancel()
        "Cancelled"
    }

    suspend fun timeoutExample(): String = try {
        withTimeout(80) {
            delay(200)
            "completed"
        }
    } catch (_: TimeoutCancellationException) {
        "Timeout happened"
    }

    suspend fun timeoutOrNullExample(): String? = withTimeoutOrNull(60) {
        delay(20)
        "Fast enough"
    }
}

@JvmName("Lesson03CancellationMain")
fun main() = runBlocking {
    println("=== 03_cancellation_and_timeouts ===")
    println(Lesson03CancellationAndTimeouts.timeoutExample())
    println(Lesson03CancellationAndTimeouts.timeoutOrNullExample())
    println(Lesson03CancellationAndTimeouts.cancellablePipeline())
}
