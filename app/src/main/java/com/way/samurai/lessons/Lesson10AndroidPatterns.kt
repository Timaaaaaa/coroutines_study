package com.way.samurai.lessons

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PseudoViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatcher + job)

    private val search = MutableStateFlow("")

    val uiState: StateFlow<String> = search
        .flatMapLatest { query ->
            flow {
                if (query.isBlank()) emit("Idle") else emit("Searching $query")
                delay(80)
                emit("Result for $query")
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, "Idle")

    fun onQueryChanged(value: String) {
        scope.launch { search.emit(value) }
    }

    fun clear() {
        job.cancel()
    }
}

object Lesson10AndroidPatternsLauncher {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("=== 10_android_patterns ===")
        val viewModel = PseudoViewModel()
        val job = launch { viewModel.uiState.collect { println("VM state: $it") } }
        viewModel.onQueryChanged("network")
        delay(200)
        viewModel.clear()
        job.cancel()
    }
}
