package com.way.samurai.lessons

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest

object Lesson09Testing {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun timeControlSample(): String = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        var value = 0
        scope.launch {
            delay(1_000)
            value = 42
        }
        advanceTimeBy(1_000)
        return@runTest "Value set to $value"
    }
}

@JvmName("Lesson09TestingMain")
fun main() = runBlocking {
    println("=== 09_testing ===")
    println(Lesson09Testing.timeControlSample())
}
