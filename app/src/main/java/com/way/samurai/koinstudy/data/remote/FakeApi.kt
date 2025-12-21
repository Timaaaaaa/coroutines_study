package com.way.samurai.koinstudy.data.remote

import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeApi {
    suspend fun getUser(): UserDto {
        delay(600)
        if (Random.nextFloat() < 0.2f) {
            error("Simulated network error")
        }
        return UserDto(
            id = "42",
            name = "Ada Lovelace",
            title = "Mathematician & First Programmer",
            bio = "Advocates structured diagrams and clean architecture.",
        )
    }
}

data class UserDto(
    val id: String,
    val name: String,
    val title: String,
    val bio: String,
)
