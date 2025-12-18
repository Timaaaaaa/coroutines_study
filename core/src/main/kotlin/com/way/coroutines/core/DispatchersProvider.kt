package com.way.coroutines.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val computation: CoroutineDispatcher get() = default
    val main: CoroutineDispatcher get() = Dispatchers.Default
}

object StandardDispatchers : DispatchersProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
