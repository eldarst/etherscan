package com.etherscan.app.model

import java.math.BigDecimal

data class TransactionEntity(
    val id: Long = 0,
    val blockNumber: Long,
    val txHash: String,
    val fromAddress: String,
    val toAddress: String?,
    val value: BigDecimal,
)
