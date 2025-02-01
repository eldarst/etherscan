package com.etherscan.app.model.client

import kotlinx.serialization.Serializable

@Serializable
data class EthereumTransaction(
    val hash: String,
    val blockNumber: String,
    val from: String,
    val to: String?,
    val value: String,
)
