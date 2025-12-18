package com.way.coroutines.examples.testing

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineExamplesTest {

    @Test
    fun `cancellation stops work`() = runTest(StandardTestDispatcher()) {
        var ticks = 0
        val job = launch {
            repeat(5) {
                delay(100)
                ticks++
            }
        }
        advanceUntilIdle()
        assertEquals(5, ticks)

        val second = launch {
            while (true) {
                delay(100)
                ticks++
            }
        }
        second.cancelAndJoin()
        assertTrue(ticks >= 5)
    }

    @Test
    fun `timeout throws CancellationException`() = runBlocking {
        var reached = false
        try {
            withTimeout(50) {
                delay(100)
                reached = true
            }
        } catch (ce: CancellationException) {
            assertTrue(!reached)
        }
    }

    @Test
    fun `supervisor keeps siblings alive`() = runTest {
        val results = mutableListOf<String>()
        supervisorScope {
            launch {
                delay(10)
                throw IllegalStateException("boom")
            }
            launch {
                delay(20)
                results += "alive"
            }
        }
        assertEquals(listOf("alive"), results)
    }

    @Test
    fun `debounce and flatMapLatest pick latest query`() = runTest(StandardTestDispatcher()) {
        val queries = MutableStateFlow("a")
        val output = mutableListOf<String>()

        val job = launch {
            queries
                .debounce(100)
                .flatMapLatest { query ->
                    flow {
                        emit("result-$query")
                    }
                }
                .toList(output)
        }

        queries.value = "ab"
        advanceUntilIdle()
        job.cancel()

        assertTrue(output.last().endsWith("ab"))
    }
}
