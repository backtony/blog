package com.example.r2dbc.utils

class AsyncLazy<T>(private val initializer: suspend () -> T?) {
    private var value: T? = null
    private var initialized = false

    suspend fun get(): T? {
        if (!initialized) {
            value = initializer()
            initialized = true
        }
        return value
    }
}

class AsyncListLazy<T>(private val initializer: suspend () -> List<T>) {
    private var value: List<T> = emptyList()
    private var initialized = false

    suspend fun get(): List<T> {
        if (!initialized) {
            value = initializer()
            initialized = true
        }
        return value
    }
}
