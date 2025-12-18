package com.way.samurai

import android.util.Log
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    Log.d("coroutines-entry", "Starting coroutine usage examples on ${Thread.currentThread().name}")
    basicLaunchAndAsyncExample()
    structuredConcurrencyExample()
    cancellationAndTimeoutExample()
    supervisorScopeExample()
}

private suspend fun basicLaunchAndAsyncExample() = coroutineScope {
    val fireAndForget = launch(Dispatchers.Default + CoroutineName("fire-and-forget")) {
        val result = slowOperation("launch-call")
        Log.d("coroutines-basic", "Launch finished with $result on ${Thread.currentThread().name}")
    }

    val userDeferred = async(Dispatchers.IO + CoroutineName("async-user")) { fetchUserProfile() }
    val configDeferred = async(Dispatchers.Default + CoroutineName("async-config")) { fetchRemoteConfig() }

    Log.d("coroutines-basic", "Awaiting async results on ${Thread.currentThread().name}")
    Log.d("coroutines-basic", "Results: ${userDeferred.await()} + ${configDeferred.await()}")
    fireAndForget.join()
}

private suspend fun structuredConcurrencyExample() = coroutineScope {
    val elapsed = measureTimeMillis {
        val user = async(Dispatchers.IO + CoroutineName("structured-user")) { fetchUserProfile() }
        val config = async(Dispatchers.Default + CoroutineName("structured-config")) { fetchRemoteConfig() }
        Log.d("coroutines-structured", "Combined value ${user.await()} with ${config.await()}")
    }

    Log.d("coroutines-structured", "Structured block finished in ${elapsed}ms on ${Thread.currentThread().name}")
}

private suspend fun cancellationAndTimeoutExample() = coroutineScope {
    val timedResult = withTimeoutOrNull(350) {
        repeat(5) { index ->
            delay(100)
            Log.d("coroutines-timeout", "Emitting value $index on ${Thread.currentThread().name}")
        }
        "Finished without timeout"
    }

    Log.d("coroutines-timeout", "Timeout result: ${timedResult ?: "Cancelled by timeout"}")
}

private suspend fun supervisorScopeExample() = supervisorScope {
    val failingJob = launch(Dispatchers.Default + CoroutineName("supervisor-failure")) {
        delay(150)
        throw IllegalStateException("simulated failure")
    }

    val resilientJob = launch(Dispatchers.Default + CoroutineName("supervisor-resilient")) {
        repeat(3) { index ->
            delay(120)
            Log.d("coroutines-supervisor", "Still running child $index on ${Thread.currentThread().name}")
        }
    }

    failingJob.invokeOnCompletion { throwable ->
        Log.d("coroutines-supervisor", "Failing child completed with ${throwable?.message}")
    }

    resilientJob.join()
}

private suspend fun slowOperation(source: String): String {
    delay(300)
    return "completed-$source"
}

private suspend fun fetchUserProfile(): String {
    delay(500)
    return "user-profile"
}

private suspend fun fetchRemoteConfig(): String {
    delay(400)
    return "remote-config"
}
