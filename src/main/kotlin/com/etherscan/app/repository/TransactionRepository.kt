package com.etherscan.app.repository

import com.etherscan.app.extensions.notNull
import com.etherscan.app.model.TransactionEntity
import com.example.generated.jooq.tables.Transactions.Companion.TRANSACTIONS
import org.jooq.Configuration
import org.jooq.Records
import org.springframework.stereotype.Repository

@Repository
class TransactionRepository(private val configuration: Configuration) {
    fun insertTransactions(
        ethereumTransactions: Collection<TransactionEntity>,
        jooqConfiguration: Configuration? = null,
    ) {
        val jooqConf = jooqConfiguration ?: configuration
        jooqConf.dsl().transaction { cfg ->
            var insertQuery =
                cfg.dsl()
                    .insertInto(
                        TRANSACTIONS,
                        TRANSACTIONS.BLOCK_NUMBER,
                        TRANSACTIONS.TX_HASH,
                        TRANSACTIONS.FROM_ADDRESS,
                        TRANSACTIONS.TO_ADDRESS,
                        TRANSACTIONS.VALUE,
                    )
            for (transaction in ethereumTransactions) {
                insertQuery =
                    insertQuery.values(
                        transaction.blockNumber,
                        transaction.txHash,
                        transaction.fromAddress,
                        transaction.toAddress,
                        transaction.value,
                    )
            }
            insertQuery
                .onConflict(TRANSACTIONS.TX_HASH)
                .doNothing()
                .execute()
        }
    }

    fun getByBlockNumbers(blockNumbers: Collection<Long>): List<TransactionEntity> {
        return configuration.dsl().select(
            TRANSACTIONS.ID.notNull().convertFrom { it.toLong() },
            TRANSACTIONS.BLOCK_NUMBER.notNull(),
            TRANSACTIONS.TX_HASH.notNull(),
            TRANSACTIONS.FROM_ADDRESS.notNull(),
            TRANSACTIONS.TO_ADDRESS.notNull(),
            TRANSACTIONS.VALUE.notNull(),
        )
            .from(TRANSACTIONS)
            .where(TRANSACTIONS.BLOCK_NUMBER.`in`(blockNumbers))
            .fetch(Records.mapping(::TransactionEntity))
    }
}
