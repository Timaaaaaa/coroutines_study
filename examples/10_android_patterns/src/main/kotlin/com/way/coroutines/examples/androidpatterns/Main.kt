package com.way.coroutines.examples.androidpatterns

import com.way.coroutines.core.DispatchersProvider
import com.way.coroutines.core.Logger
import com.way.coroutines.core.StandardDispatchers
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.SupervisorJob

fun main() = runBlocking(CoroutineName("10-android-patterns")) {
    val viewModel = SearchViewModel(StandardDispatchers, Logger("10-viewmodel"))
    val job = launch {
        viewModel.state.collectLatest { println("UI renders: $it") }
    }
    viewModel.onSearchQuery("kot")
    viewModel.onSearchQuery("kotlin")
    kotlinx.coroutines.delay(300)
    job.cancel()
    viewModel.clear()
}

class SearchViewModel(
    private val dispatchers: DispatchersProvider,
    private val logger: Logger,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatchers.default + job + CoroutineName("search-vm"))

    private val _state = MutableStateFlow("Idle")
    val state: StateFlow<String> = _state.asStateFlow()

    fun onSearchQuery(query: String) {
        _state.value = "Typing..."
        scope.launch {
            _state.value = "Searching for $query"
            searchFlow(query).collectLatest { result ->
                _state.value = result
            }
        }
    }

    private fun searchFlow(query: String) =
        MutableStateFlow(query)
            .debounce(150)
            .flatMapLatest { latest ->
                flow {
                    logger.info("Simulating search for $latest on ${Thread.currentThread().name}")
                    emit("Result for $latest")
                }
            }

    fun clear() {
        scope.cancel()
    }
}
