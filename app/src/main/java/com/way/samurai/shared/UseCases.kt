package com.way.samurai.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UseCases(private val repository: Repository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val currentQuery = MutableStateFlow("")

    fun observePagedSearch(): Flow<List<Article>> = repository
        .observeSearch(currentQuery)
        .onEach { println("Search emitted ${it.size} items") }

    suspend fun reloadPageWithCache(page: Int): List<Article> = coroutineScope {
        val cache = async { repository.loadPage(page) }
        val enriched = async { repository.enrichArticles(page) }
        cache.await() + enriched.await().map { (article, details) ->
            article.copy(body = article.body + " (" + details.summary + ")")
        }
    }

    fun paginationFlow(): Flow<List<Article>> = flow {
        var page = 0
        while (page < 3) {
            emit(repository.loadPage(page))
            page++
        }
    }

    fun pageState(): StateFlow<List<Article>> = paginationFlow().stateIn(
        scope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    fun runFanOutFanIn(ids: List<Int>): Flow<ArticleDetails> = flow {
        coroutineScope {
            ids.chunked(3).forEach { batch ->
                batch.map { id ->
                    async { repository.enrichArticles(id / 100).first().second }
                }.awaitAll().forEach { emit(it) }
            }
        }
    }

    suspend fun updateQuery(query: String) = withContext(scope.coroutineContext) {
        currentQuery.value = query
    }
}
