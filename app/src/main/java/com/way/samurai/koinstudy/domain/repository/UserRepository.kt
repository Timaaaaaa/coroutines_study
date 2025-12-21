package com.way.samurai.koinstudy.domain.repository

import com.way.samurai.koinstudy.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val userStream: Flow<UserState>
    suspend fun refresh()
}

sealed interface UserState {
    data object Idle : UserState
    data object Loading : UserState
    data class Data(val user: User) : UserState
    data class Error(val message: String) : UserState
}
