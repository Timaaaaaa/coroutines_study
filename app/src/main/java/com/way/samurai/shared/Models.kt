package com.way.samurai.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Article(
    val id: Int,
    val title: String,
    val body: String,
    val page: Int,
)

data class ArticleDetails(
    val id: Int,
    val summary: String,
    val author: String,
)

data class UserPreferences(
    val showDebug: Boolean = false,
)

class InMemoryCache<T>(initialValue: T) {
    private val state = MutableStateFlow(initialValue)
    val flow: StateFlow<T> get() = state

    fun update(value: T) {
        state.value = value
    }
}
