package com.way.samurai

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CoroutinesPlaygroundTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelling parent cancels children`() = runTest {
        val parent = launch {
            coroutineScope {
                launch { delay(10_000) }
            }
        }

        parent.cancelAndJoin()
        assertTrue(parent.isCancelled)
    }

    @Test
    fun `withTimeout cancels work`() = runTest {
        assertFailsWith<TimeoutCancellationException> {
            withTimeout(10) { delay(100) }
        }
    }

    @Test
    fun `supervisor keeps siblings alive`() = runTest {
        var survivorRan = false
        supervisorScope {
            launch { throw IllegalStateException("boom") }
            launch { survivorRan = true }
        }
        assertTrue(survivorRan)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `debounce with flatMapLatest respects latest query`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val queries = MutableStateFlow("a")

        val results = mutableListOf<String>()
        val job = scope.launch {
            queries
                .debounce(100)
                .flatMapLatest { query -> flowOf("result for $query") }
                .collect { results.add(it) }
        }

        queries.value = "ab"
        testScheduler.advanceTimeBy(200)
        queries.value = "abc"
        testScheduler.advanceTimeBy(200)
        scope.coroutineContext.job.cancel()
        job.cancelAndJoin()

        assertEquals(listOf("result for ab", "result for abc"), results)
    }
}
