package com.way.coroutines.core.data

import com.way.coroutines.core.AppResult
import com.way.coroutines.core.DispatchersProvider
import com.way.coroutines.core.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class Repository(
    private val api: FakeApi,
    private val db: FakeDb,
    private val dispatchers: DispatchersProvider,
    private val logger: Logger,
) {
    private val userState = MutableStateFlow<User?>(null)
    private val articlesState = MutableStateFlow<List<Article>>(emptyList())

    val users: Flow<User> = userState.filterNotNull()
    val articles: StateFlow<List<Article>> = articlesState

    suspend fun refreshUser(id: String): AppResult<User> = coroutineScope {
        val cached = db.readUser(id)
        if (cached is AppResult.Success && cached.value != null) {
            userState.update { cached.value }
        }

        val result = api.loadUser(id)
        if (result is AppResult.Success) {
            userState.update { result.value }
            db.upsertUser(result.value)
        }
        result
    }

    suspend fun refreshConfigAndUser(id: String): AppResult<Pair<User, Config>> = coroutineScope {
        val (userResult, configResult) = listOf(
            async(dispatchers.io) { api.loadUser(id) },
            async(dispatchers.io) { api.loadConfig() },
        ).awaitAll()

        if (userResult is AppResult.Success) db.upsertUser(userResult.value)
        when {
            userResult is AppResult.Success && configResult is AppResult.Success ->
                AppResult.Success(userResult.value to configResult.value)
            userResult is AppResult.Error -> userResult
            configResult is AppResult.Error -> configResult
            else -> AppResult.Error(IllegalStateException("Unknown state"))
        }
    }

    suspend fun refreshArticles(page: Int): AppResult<List<Article>> = coroutineScope {
        val result = api.loadArticles(page)
        if (result is AppResult.Success) {
            db.saveArticles(result.value)
            articlesState.update { existing -> (existing + result.value).distinctBy { it.id } }
        }
        result
    }

    suspend fun readFromCache(): AppResult<Pair<User?, List<Article>>> = withContext(dispatchers.io) {
        val user = db.readUser("primary")
        val articles = db.readArticles()
        when {
            user is AppResult.Error -> user
            articles is AppResult.Error -> articles
            else -> AppResult.Success((user as AppResult.Success).value to (articles as AppResult.Success).value)
        }
    }

    suspend fun paginateSafely(
        page: Int,
        allowCacheFallback: Boolean = true,
    ): AppResult<List<Article>> = coroutineScope {
        val cached = if (allowCacheFallback) db.readArticles() else null
        val network = refreshArticles(page)
        if (network is AppResult.Success) return@coroutineScope network
        logger.warn("Network failed, cache fallback: ${(network as? AppResult.Error)?.throwable?.message}")
        cached ?: network
    }
}
