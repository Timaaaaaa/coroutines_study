package com.way.samurai.koinstudy.domain.model

data class User(
    val id: String,
    val name: String,
    val title: UserTitle,
    val bio: UserBio,
)

@JvmInline
value class UserTitle(val value: String)

@JvmInline
value class UserBio(val value: String)
