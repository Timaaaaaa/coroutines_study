package com.way.samurai.koinstudy.domain.usecase

import com.way.samurai.koinstudy.domain.repository.UserRepository

class RefreshUserUseCase(
    private val repository: UserRepository,
) {
    suspend operator fun invoke() = repository.refresh()
}
