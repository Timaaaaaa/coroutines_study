package com.way.samurai.lessons

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

object Lesson07ConcurrencyPrimitives {
    private val mutex = Mutex()
    private val balance = AtomicInteger(0)
    private val rateLimiter = Semaphore(permits = 2)

    suspend fun criticalSectionDemo() = coroutineScope {
        repeat(5) {
            launch {
                mutex.withLock {
                    val after = balance.incrementAndGet()
                    println("Protected increment to $after")
                }
            }
        }
    }

    suspend fun concurrencyLimitDemo(urls: List<String>) = coroutineScope {
        urls.map { url ->
            launch {
                rateLimiter.withPermit {
                    delay(40)
                    println("Fetched $url with max 2 concurrent")
                }
            }
        }.forEach { it.join() }
    }

    suspend fun rateLimitDemo() = coroutineScope {
        val semaphore = Semaphore(permits = 3)
        repeat(6) { index ->
            launch {
                semaphore.withPermit {
                    delay(30)
                    println("Permit used for task $index")
                }
            }
        }
    }
}

@JvmName("Lesson07ConcurrencyPrimitivesMain")
fun main() = runBlocking {
    println("=== 07_concurrency_primitives ===")
    Lesson07ConcurrencyPrimitives.criticalSectionDemo()
    Lesson07ConcurrencyPrimitives.concurrencyLimitDemo(listOf("/users", "/posts", "/comments"))
    Lesson07ConcurrencyPrimitives.rateLimitDemo()
}
