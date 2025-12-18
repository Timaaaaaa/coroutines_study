package com.way.samurai.shared

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(
    private val api: FakeApi,
    private val db: FakeDb,
    private val cache: InMemoryCache<Map<Int, List<Article>>>,
    private val rateLimiter: RateLimiter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val scope = CoroutineScope(ioDispatcher + kotlinx.coroutines.SupervisorJob())

    private val searchQueries = MutableSharedFlow<String>(extraBufferCapacity = 10)
    private val preferences = MutableStateFlow(UserPreferences())

    suspend fun loadPage(page: Int): List<Article> = coroutineScope {
        cache.flow.value[page]?.let { return@coroutineScope it }

        val cached = db.readPage(page)
        if (cached != null) {
            cache.update(cache.flow.value + (page to cached))
            return@coroutineScope cached
        }

        try {
            val networkItems = rateLimiter.withPermit { api.fetchPage(page) }
            db.writePage(page, networkItems)
            cache.update(cache.flow.value + (page to networkItems))
            networkItems
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            cache.flow.value[page] ?: throw error
        }
    }

    suspend fun enrichArticles(page: Int): List<Pair<Article, ArticleDetails>> = coroutineScope {
        val articles = loadPage(page)
        articles
            .map { article ->
                async { article to api.fetchArticleDetails(article.id) }
            }
            .awaitAll()
    }

    fun observeSearch(queryFlow: StateFlow<String>) = queryFlow
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                emit(rateLimiter.withPermit { api.search(query) })
            }
        }
        .flowOn(ioDispatcher)

    fun observeUiState(): StateFlow<UiState> = combine(
        cache.flow,
        preferences,
    ) { pages, prefs ->
        UiState(pages = pages, showDebug = prefs.showDebug)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = UiState(emptyMap(), false),
    )

    suspend fun toggleDebug(flag: Boolean) = withContext(ioDispatcher) {
        preferences.value = preferences.value.copy(showDebug = flag)
    }

    data class UiState(val pages: Map<Int, List<Article>>, val showDebug: Boolean)

    suspend fun emitSearch(query: String) {
        searchQueries.emit(query)
    }

    fun suggestionsFlow() = searchQueries
        .debounce(150)
        .map { query -> query.take(15) }
        .flowOn(ioDispatcher)

    fun concurrencyLimitedRequests(urls: List<String>, maxParallel: Int = 3) = channelFlow {
        val semaphore = kotlinx.coroutines.sync.Semaphore(maxParallel)
        urls.forEach { url ->
            launch {
                semaphore.withPermit { send("Fetched $url") }
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
