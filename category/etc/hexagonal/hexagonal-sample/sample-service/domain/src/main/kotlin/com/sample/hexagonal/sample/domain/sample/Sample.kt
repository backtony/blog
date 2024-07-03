package com.sample.hexagonal.sample.domain.sample

import java.time.LocalDateTime

class Sample(
    val id: String? = null,
    name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {

    var name: String = name
        private set

    var updatedAt: LocalDateTime = updatedAt
        private set

    fun update(name: String): Sample {
        this.name = name
        this.updatedAt = LocalDateTime.now()

        return this
    }

    companion object {
        fun create(name: String): Sample {
            return Sample(
                name = name,
            )
        }
    }
}
