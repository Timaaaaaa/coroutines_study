package com.way.samurai.koinstudy.presentation.state

import com.way.samurai.koinstudy.domain.model.User

sealed interface UserUiState {
    data object Idle : UserUiState
    data object Loading : UserUiState
    data class Content(val user: User) : UserUiState
    data class Error(val message: String) : UserUiState
}
