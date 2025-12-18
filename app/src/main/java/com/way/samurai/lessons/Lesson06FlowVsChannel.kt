package com.way.samurai.lessons

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object Lesson06FlowVsChannel {
    private val uiState = MutableStateFlow("Idle")
    private val events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val taskQueue = Channel<Int>(capacity = Channel.BUFFERED)

    suspend fun runDemo() = coroutineScope {
        launch {
            uiState.collectLatest { println("StateFlow (UI state): $it") }
        }
        launch {
            events.collect { println("SharedFlow (event): $it") }
        }
        launch {
            taskQueue.consumeEachWithBackpressure()
        }

        uiState.value = "Loading"
        events.emit("Button clicked")
        repeat(5) { index -> taskQueue.send(index) }
        taskQueue.close()
        uiState.value = "Loaded"
    }

    private suspend fun Channel<Int>.consumeEachWithBackpressure() {
        for (item in this) {
            delay(30)
            println("Channel task processed: $item")
        }
    }

    fun coldFlow(): kotlinx.coroutines.flow.Flow<Int> = flow {
        repeat(3) { emit(it) }
    }

    suspend fun bufferVsConflate() = coroutineScope {
        launch {
            coldFlow().buffer(2).collect { println("buffered: $it") }
        }
        launch {
            coldFlow().conflate().collect { println("conflated: $it") }
        }
    }
}

@JvmName("Lesson06FlowVsChannelMain")
fun main() = runBlocking {
    println("=== 06_flow_vs_channel ===")
    Lesson06FlowVsChannel.runDemo()
    Lesson06FlowVsChannel.bufferVsConflate()
}
