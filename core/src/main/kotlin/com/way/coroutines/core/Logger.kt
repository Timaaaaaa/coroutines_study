package com.way.coroutines.core

class Logger(private val tag: String) {
    fun info(message: String) = println("[$tag] $message")
    fun warn(message: String) = println("[$tag][warn] $message")
    fun error(message: String, throwable: Throwable? = null) {
        println("[$tag][error] $message")
        throwable?.printStackTrace()
    }
}
