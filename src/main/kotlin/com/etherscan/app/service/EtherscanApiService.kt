package com.etherscan.app.service

import com.etherscan.app.model.TransactionEntity

interface EtherscanApiService {
    suspend fun getBlockTransactions(
        blockNumber: Long,
        longRunning: Boolean = false,
    ): Collection<TransactionEntity>?

    suspend fun getLatestBlockNumber(): Long?
}
