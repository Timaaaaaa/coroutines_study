package com.way.coroutines.core.data

data class User(val id: String, val name: String)
data class Config(val featureFlags: Map<String, Boolean>)
data class Article(val id: String, val title: String)
