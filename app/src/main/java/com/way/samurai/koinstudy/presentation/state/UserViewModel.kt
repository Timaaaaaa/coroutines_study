package com.way.samurai.koinstudy.presentation.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.way.samurai.koinstudy.data.dispatchers.DispatcherProvider
import com.way.samurai.koinstudy.domain.repository.UserState
import com.way.samurai.koinstudy.domain.usecase.GetUserUseCase
import com.way.samurai.koinstudy.domain.usecase.RefreshUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(
    private val getUser: GetUserUseCase,
    private val refreshUser: RefreshUserUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState

    init {
        observeUser()
    }

    fun load() {
        viewModelScope.launch(dispatchers.main) {
            refreshUser()
        }
    }

    private fun observeUser() {
        viewModelScope.launch(dispatchers.main) {
            getUser().collectLatest { state ->
                _uiState.value = state.toUi()
            }
        }
    }

    private fun UserState.toUi(): UserUiState = when (this) {
        UserState.Idle -> UserUiState.Idle
        UserState.Loading -> UserUiState.Loading
        is UserState.Data -> UserUiState.Content(user)
        is UserState.Error -> UserUiState.Error(message)
    }
}
