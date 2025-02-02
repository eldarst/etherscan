package com.etherscan.app.repository

import com.etherscan.app.model.TransactionEntity
import org.jooq.Configuration

interface TransactionRepository {
    fun insertTransactions(
        ethereumTransactions: Collection<TransactionEntity>,
        jooqConfiguration: Configuration? = null,
    )

    fun getByBlockNumbers(blockNumbers: Collection<Long>): List<TransactionEntity>
}
