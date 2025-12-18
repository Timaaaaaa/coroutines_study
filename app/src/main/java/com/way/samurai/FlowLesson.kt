package com.way.samurai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
            flowBuilder1().collect {
                println(it + "-------" + Thread.currentThread().name)
            }
    }

    val j = GlobalScope.launch {
        flowBuilder1().collect{
            println(it + "111111")
        }
    }

}

/**
 * simple builder of flow
 */
fun flowBuilder1() = flow {
    val names = listOf("Jody", "Steve", "Lance", "Joe")
    for (name in names) {
        delay(100)
        emit(name)
    }
}.flowOn(Dispatchers.IO)