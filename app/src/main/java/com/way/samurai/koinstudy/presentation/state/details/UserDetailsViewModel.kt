package com.way.samurai.koinstudy.presentation.state.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.way.samurai.koinstudy.domain.repository.UserState
import com.way.samurai.koinstudy.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserDetailsViewModel(
    private val userId: String,
    private val getUser: GetUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UserState>(UserState.Idle)
    val state: StateFlow<UserState> = _state

    init {
        // just observe data to show how parametersOf works in Koin
        viewModelScope.launch {
            getUser().collectLatest { _state.value = it }
        }
    }

    fun currentUserId(): String = userId
}
