package com.example.r2dbc.utils

fun <R> memoizeSuspendNullable(func: suspend () -> R?): (suspend () -> R?) {
    var initialized = false
    var result: R? = null

    return {
        if (!initialized) {
            result = func()
            initialized = true
        }
        result
    }
}
