package com.way.coroutines.core.data

import com.way.coroutines.core.AppResult
import com.way.coroutines.core.runCatchingResult
import kotlinx.coroutines.delay

class FakeDb {
    private val users = mutableMapOf<String, User>()
    private val articles = mutableMapOf<String, Article>()

    suspend fun upsertUser(user: User): AppResult<Unit> = runCatchingResult {
        delay(50)
        users[user.id] = user
    }

    suspend fun readUser(id: String): AppResult<User?> = runCatchingResult {
        delay(30)
        users[id]
    }

    suspend fun saveArticles(items: List<Article>): AppResult<Unit> = runCatchingResult {
        delay(40)
        items.forEach { articles[it.id] = it }
    }

    suspend fun readArticles(): AppResult<List<Article>> = runCatchingResult {
        delay(20)
        articles.values.sortedBy { it.id }
    }
}
