package com.way.samurai.shared

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class RateLimiter(private val permits: Int, private val burst: Int = permits) {
    private val semaphore = Semaphore(burst)

    suspend fun <T> withPermit(block: suspend () -> T): T {
        return semaphore.withPermit { block() }
    }
}
