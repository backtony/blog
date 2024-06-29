package com.example.springgraphql.client.common

import com.example.proto.common.IdRequest
import com.example.proto.common.idRequest

object CommonMapper {

    fun generateIdRequest(id: Long): IdRequest {
        return idRequest {
            this.id = id
        }
    }
}
