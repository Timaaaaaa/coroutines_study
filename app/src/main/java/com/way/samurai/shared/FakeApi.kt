package com.way.samurai.shared

import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeApi(
    private val random: Random = Random.Default,
    private val latencyMs: Long = 120,
) {
    suspend fun fetchPage(page: Int, pageSize: Int = 5): List<Article> {
        delay(latencyMs)
        maybeFail()
        return (0 until pageSize).map { index ->
            val id = page * 100 + index
            Article(
                id = id,
                title = "Article #$id",
                body = "Server content for page $page / item $index",
                page = page,
            )
        }
    }

    suspend fun fetchArticleDetails(id: Int): ArticleDetails {
        delay(latencyMs / 2)
        maybeFail(failureRate = 0.15)
        return ArticleDetails(
            id = id,
            summary = "Details for article $id",
            author = "Author-${id % 4}",
        )
    }

    suspend fun search(query: String): List<Article> {
        delay(latencyMs)
        if (query.isBlank()) return emptyList()
        return fetchPage(query.hashCode().absoluteValue % 3)
    }

    private fun maybeFail(failureRate: Double = 0.2) {
        if (random.nextDouble() < failureRate) {
            throw IllegalStateException("Network glitch while calling FakeApi")
        }
    }

    private val Int.absoluteValue: Int get() = if (this < 0) -this else this
}
