package com.way.samurai

import android.util.Log
import kotlinx.coroutines.*

fun main() {
    runBlocking {

        val call = launch { call1() }
        val call1 = async { call1() }
        val call2 = async { call2() }
        Log.d("current thread name", "onCreate: ${Thread.currentThread().name}")
        Log.d("current thread name", "onCreate: ${call1.await()} ${call2.await()}")
        Log.d("current thread name", "onCreate: ${call}")
    }
}

