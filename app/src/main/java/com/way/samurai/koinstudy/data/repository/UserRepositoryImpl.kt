package com.way.samurai.koinstudy.data.repository

import com.way.samurai.koinstudy.data.dispatchers.DispatcherProvider
import com.way.samurai.koinstudy.data.remote.TaskRemoteDataSource
import com.way.samurai.koinstudy.domain.repository.UserRepository
import com.way.samurai.koinstudy.domain.repository.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val remoteDataSource: TaskRemoteDataSource,
    private val dispatchers: DispatcherProvider,
) : UserRepository {

    private val scope = CoroutineScope(dispatchers.io + Job())
    private val state = MutableStateFlow<UserState>(UserState.Idle)

    override val userStream: StateFlow<UserState> = state.asStateFlow()

    override suspend fun refresh() {
        state.value = UserState.Loading
        runCatching {
            withContext(dispatchers.io) {
                remoteDataSource.fetchUser()
            }
        }.onSuccess { user ->
            state.value = UserState.Data(user)
        }.onFailure { throwable ->
            state.value = UserState.Error(throwable.message.orEmpty())
        }
    }

    fun preload() {
        scope.launch { refresh() }
    }
}
