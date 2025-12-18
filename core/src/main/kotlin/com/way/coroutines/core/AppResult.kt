package com.way.coroutines.core

sealed class AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>()
    data class Error(val throwable: Throwable) : AppResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Error -> this
    }
}

inline fun <T> runCatchingResult(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (t: Throwable) {
        if (t is java.util.concurrent.CancellationException) throw t
        AppResult.Error(t)
    }
