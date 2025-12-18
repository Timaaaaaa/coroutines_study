package com.way.samurai.lessons

import com.way.samurai.shared.FakeApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

object Lesson04StructuredConcurrency {
    private val api = FakeApi()

    suspend fun fanOutFanIn(page: Int): List<String> = coroutineScope {
        val articles = api.fetchPage(page)
        val detailJobs = articles.map { article ->
            async { article.title to api.fetchArticleDetails(article.id) }
        }
        detailJobs.awaitAll().map { (title, details) -> "$title | ${details.summary}" }
    }
}

object Lesson04StructuredLauncher {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("=== 04_structured_concurrency ===")
        Lesson04StructuredConcurrency.fanOutFanIn(1).forEach { println(it) }
    }
}
