package com.example.r2dbc.utils

import org.springframework.data.domain.Sort
import org.springframework.r2dbc.core.DatabaseClient

fun String.plusWhere(conditions: Map<String, Any?>): String {
    if (conditions.isEmpty()) {
        return this
    }

    return "$this\nWHERE ${conditions.keys.joinToString(" AND ")}"
}

fun String.plusGroupBy(vararg columns: String): String {
    return "$this\nGROUP BY ${columns.joinToString(",")}"
}

fun String.plusHaving(conditions: Map<String, Any?>): String {
    if (conditions.isEmpty()) {
        return this
    }

    return "$this\nHAVING ${conditions.keys.joinToString(" AND ")}"
}

// TODO orderby와 sort는 바인딩 지원 안함
fun String.plusOrderBy(conditions: List<Pair<String, Sort.Direction>>): String {

    val orderByClause = conditions.joinToString(", ") { (field, direction) ->
        "${field.toSnakeCase()} ${direction.name}"
    }

    return if (orderByClause.isNotEmpty()) {
        "$this\nORDER BY $orderByClause"
    } else {
        this
    }
}

fun String.toSnakeCase(): String {
    return this.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
}

fun String.plusPagination(page: Int?, size: Int?): String {
    if (page == null && size == null) {
        return this
    }

    if (page != null && size != null) {
        return "$this\nLIMIT :page, :size"
    }

    return "$this\nLIMIT :size"
}

fun DatabaseClient.GenericExecuteSpec.bindConditions(
    conditions: Map<String, Any?>,
): DatabaseClient.GenericExecuteSpec {
    val source = conditions.entries.mapNotNull { (condition, value) ->
        val param = Regex(":(\\w+)").find(condition)?.value?.removePrefix(":")
        if (param == null) {
            null
        } else {
            param to value
        }
    }.toMap()

    return this.bindValues(source)
}

fun DatabaseClient.GenericExecuteSpec.bindPage(
    page: Int,
    size: Int,
): DatabaseClient.GenericExecuteSpec {
    var spec = this
    spec = spec.bind("page", (page - 1) * size)
    spec = spec.bind("size", size)
    return spec
}
