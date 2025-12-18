package com.way.coroutines.core.data

import com.way.coroutines.core.AppResult
import com.way.coroutines.core.runCatchingResult
import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeApi(private val random: Random = Random(0)) {
    suspend fun loadUser(id: String): AppResult<User> = runCatchingResult {
        simulatedDelay()
        maybeThrow()
        User(id, "User-$id")
    }

    suspend fun loadConfig(): AppResult<Config> = runCatchingResult {
        simulatedDelay()
        Config(mapOf("newDashboard" to random.nextBoolean()))
    }

    suspend fun loadArticles(page: Int): AppResult<List<Article>> = runCatchingResult {
        simulatedDelay()
        List(5) { index -> Article("${page}_$index", "Article ${page * 5 + index}") }
    }

    private suspend fun simulatedDelay() = delay(150 + random.nextLong(100))

    private fun maybeThrow() {
        if (random.nextInt(0, 5) == 0) throw IllegalStateException("Network glitch")
    }
}
