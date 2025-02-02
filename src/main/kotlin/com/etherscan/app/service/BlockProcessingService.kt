package com.etherscan.app.service

import com.etherscan.app.model.TransactionEntity

interface BlockProcessingService {
    fun processBlockTransactions(
        blockTransactions: Collection<TransactionEntity>,
        blockNumber: Long,
    )

    fun processFailedBlockTransactions(
        blockTransactions: Collection<TransactionEntity>,
        blockNumber: Long,
    )

    fun addFailedBlock(blockNumber: Long)

    fun findLastProcessedBlock(): Long?

    fun findAllFailedBlock(): List<Long>
}
