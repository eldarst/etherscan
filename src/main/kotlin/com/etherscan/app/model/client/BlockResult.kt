package com.etherscan.app.model.client

import kotlinx.serialization.Serializable

@Serializable
data class BlockResult(
    val number: String,
    val transactions: List<EthereumTransaction>,
)
