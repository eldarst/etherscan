package com.etherscan.app.model.client

import kotlinx.serialization.Serializable

@Serializable
data class BlockResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T,
)
