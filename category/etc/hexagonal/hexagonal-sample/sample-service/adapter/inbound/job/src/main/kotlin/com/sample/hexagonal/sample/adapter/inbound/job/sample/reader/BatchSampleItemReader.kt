package com.sample.hexagonal.sample.adapter.inbound.job.sample.reader

import org.springframework.batch.item.database.AbstractPagingItemReader
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

open class BatchSampleItemReader(
    chunkSize: Int,
) : AbstractPagingItemReader<String>() {

    val data = MutableList(1000) { UUID.randomUUID().toString() }

    init {
        pageSize = chunkSize
    }

    override fun doReadPage() {
        if (results == null) {
            results = CopyOnWriteArrayList()
        } else {
            results.clear()
        }

        val uuids = data.take(100)
        results.addAll(uuids)
        data.removeAll(uuids)
    }

    override fun getPage(): Int {
        return 1
    }
}
