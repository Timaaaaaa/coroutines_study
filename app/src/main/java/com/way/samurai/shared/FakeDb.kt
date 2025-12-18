package com.way.samurai.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeDb(private val latencyMs: Long = 40L) {
    private val mutex = Mutex()
    private val articlesByPage: MutableMap<Int, List<Article>> = mutableMapOf()

    suspend fun readPage(page: Int): List<Article>? = mutex.withLock {
        delay(latencyMs)
        articlesByPage[page]
    }

    suspend fun writePage(page: Int, items: List<Article>) = mutex.withLock {
        delay(latencyMs)
        articlesByPage[page] = items
    }
}
