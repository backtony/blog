package com.sample.hexagonal.common.exception

inline fun <reified P> P?.getOrThrowNotFound(resourceId: Any): P {
    if (this == null) {
        throw ResourceNotFoundException.notFound(P::class.java.simpleName, resourceId.toString())
    }
    return this
}

inline fun <reified P> P?.getOrThrowNotFound(resourceIds: List<String>): P {
    if (this == null) {
        throw ResourceNotFoundException.notFound(P::class.java.simpleName, resourceIds)
    }
    return this
}
