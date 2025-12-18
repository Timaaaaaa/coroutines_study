package com.way.coroutines.examples.flowchannel

import com.way.coroutines.core.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking(CoroutineName("06-flow-channel")) {
    val logger = Logger("06-flow")
    coldFlowExample(logger)
    sharedFlowExample(logger)
    channelExample(logger)
}

private suspend fun coldFlowExample(logger: Logger) {
    val source = flow {
        listOf("A", "B", "C").forEach {
            delay(50)
            emit(it)
        }
    }

    source.onEach { logger.info("cold flow emits $it") }.collectLatest { value ->
        delay(80)
        logger.info("collector received $value")
    }
}

private suspend fun sharedFlowExample(logger: Logger) {
    val state = MutableStateFlow(0)
    val events = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val job = launch(CoroutineName("ui")) {
        state.collectLatest { logger.info("UI state = $it") }
    }

    val eventJob = launch(CoroutineName("events")) {
        events.collectLatest { logger.info("UI event: $it") }
    }

    repeat(3) {
        state.emit(it + 1)
        events.emit("Clicked #$it")
    }

    job.cancel()
    eventJob.cancel()
}

private suspend fun channelExample(logger: Logger) {
    val channel = Channel<String>(capacity = 2)
    val consumer = launch(CoroutineName("channel-consumer")) {
        for (item in channel) {
            logger.info("channel received $item")
            delay(60)
        }
    }

    launch(CoroutineName("channel-producer")) {
        repeat(5) { index ->
            channel.send("task-$index")
        }
        channel.close()
    }

    consumer.join()
}
