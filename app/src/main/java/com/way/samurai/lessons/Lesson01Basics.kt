package com.way.samurai.lessons

import com.way.samurai.shared.FakeApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object Lesson01Basics {
    private val api = FakeApi()

    suspend fun sequentialCalls() {
        val first = api.fetchPage(page = 0)
        val second = api.fetchArticleDetails(first.first().id)
        println("Sequential call summary: ${second.summary}")
    }

    suspend fun structuredLaunch() = kotlinx.coroutines.coroutineScope {
        val job = launch {
            delay(50)
            println("Child coroutine finished without leaking scope")
        }
        job.join()
    }
}

@JvmName("Lesson01BasicsMain")
fun main() = runBlocking {
    println("=== 01_basics ===")
    Lesson01Basics.sequentialCalls()
    Lesson01Basics.structuredLaunch()
}
