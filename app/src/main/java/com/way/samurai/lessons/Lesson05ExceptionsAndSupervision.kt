package com.way.samurai.lessons

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

object Lesson05ExceptionsAndSupervision {
    suspend fun childFailureCancelsParent() = try {
        coroutineScope {
            launch { throw IllegalStateException("boom") }
            launch { println("I will be cancelled because my sibling failed") }
        }
    } catch (t: Throwable) {
        println("Parent cancelled: ${t.message}")
    }

    suspend fun supervisorIsolatesFailure() = supervisorScope {
        val handler = CoroutineExceptionHandler { _, throwable ->
            println("Handled at supervisor: ${throwable.message}")
        }
        val failing = launch(handler) { throw IllegalArgumentException("ignored child failure") }
        val survivor = launch { println("Sibling keeps running") }
        failing.join()
        survivor.join()
    }

    suspend fun supervisorJobAttachedScope() {
        val job = SupervisorJob()
        val scope = kotlinx.coroutines.CoroutineScope(job)
        val task = scope.launch { throw IllegalStateException("handled by handler") }
        task.cancelAndJoin()
        job.cancel()
    }
}

object Lesson05ExceptionsLauncher {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("=== 05_exceptions_supervision ===")
        Lesson05ExceptionsAndSupervision.childFailureCancelsParent()
        Lesson05ExceptionsAndSupervision.supervisorIsolatesFailure()
        Lesson05ExceptionsAndSupervision.supervisorJobAttachedScope()
    }
}
