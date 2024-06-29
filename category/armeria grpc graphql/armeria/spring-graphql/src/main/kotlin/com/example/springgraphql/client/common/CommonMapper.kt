package com.example.springgraphql.client.common

import com.example.proto.common.IdRequest
import com.example.proto.common.IdsRequest
import com.example.proto.common.idRequest
import com.example.proto.common.idsRequest

object CommonMapper {

    fun generateIdRequest(id: Long): IdRequest {
        return idRequest {
            this.id = id
        }
    }

    fun generateIdsRequest(ids: List<Long>): IdsRequest {
        return idsRequest {
            id.addAll(ids)
        }
    }
}
