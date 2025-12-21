package com.way.samurai.koinstudy.data.remote

import com.way.samurai.koinstudy.domain.model.User
import com.way.samurai.koinstudy.domain.model.UserBio
import com.way.samurai.koinstudy.domain.model.UserTitle

class TaskRemoteDataSource(
    private val api: FakeApi,
) {
    suspend fun fetchUser(): User = api.getUser().toDomain()

    private fun UserDto.toDomain(): User = User(
        id = id,
        name = name,
        title = UserTitle(title),
        bio = UserBio(bio),
    )
}
