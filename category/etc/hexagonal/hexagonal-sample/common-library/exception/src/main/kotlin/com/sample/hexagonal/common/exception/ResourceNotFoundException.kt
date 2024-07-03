package com.sample.hexagonal.common.exception

class ResourceNotFoundException(s: String) : RuntimeException(s) {
    companion object {
        fun notFound(resourceName: String, resourceId: String): ResourceNotFoundException {
            return ResourceNotFoundException("$resourceName not found. id: $resourceId")
        }

        fun notFound(resourceName: String, resourceIds: List<String>): ResourceNotFoundException {
            return ResourceNotFoundException("$resourceName not found. ids:" + resourceIds.joinToString(","))
        }
    }
}
