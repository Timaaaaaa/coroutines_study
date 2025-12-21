package com.way.samurai.koinstudy.domain.usecase

import com.way.samurai.koinstudy.domain.repository.UserRepository

class GetUserUseCase(
    private val repository: UserRepository,
) {
    operator fun invoke() = repository.userStream
}
