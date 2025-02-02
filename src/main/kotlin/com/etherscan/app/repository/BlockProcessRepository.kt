package com.etherscan.app.repository

import com.etherscan.app.extensions.notNull
import com.example.generated.jooq.tables.FailedBlocks.Companion.FAILED_BLOCKS
import com.example.generated.jooq.tables.ProcessedBlocks.Companion.PROCESSED_BLOCKS
import org.jooq.Configuration
import org.springframework.stereotype.Repository

@Repository
class BlockProcessRepository(private val configuration: Configuration) {
    fun insertProcessedBlock(
        blockNumber: Long,
        jooqConfiguration: Configuration? = null,
    ) {
        val jooqConf = jooqConfiguration ?: configuration

        jooqConf.dsl().insertInto(PROCESSED_BLOCKS)
            .set(PROCESSED_BLOCKS.BLOCK_NUMBER, blockNumber)
            .onConflict(PROCESSED_BLOCKS.BLOCK_NUMBER)
            .doNothing()
            .execute()
    }

    fun findNProcessedBlocks(n: Long = 1, isAscending: Boolean = false): Collection<Long> {
        return configuration.dsl().select(PROCESSED_BLOCKS.BLOCK_NUMBER)
            .from(PROCESSED_BLOCKS)
            .orderBy(
                if (isAscending) {
                    PROCESSED_BLOCKS.BLOCK_NUMBER.asc()
                } else {
                    PROCESSED_BLOCKS.BLOCK_NUMBER.desc()
                }
            )
            .limit(n)
            .fetch(PROCESSED_BLOCKS.BLOCK_NUMBER.notNull())
    }

    fun insertFailedBlock(blockNumber: Long) {
        configuration.dsl().insertInto(FAILED_BLOCKS)
            .set(FAILED_BLOCKS.BLOCK_NUMBER, blockNumber)
            .onConflict(FAILED_BLOCKS.BLOCK_NUMBER)
            .doNothing()
            .execute()
    }

    fun findAllFailedBlock(): List<Long> {
        return configuration.dsl().select(FAILED_BLOCKS.BLOCK_NUMBER)
            .from(FAILED_BLOCKS)
            .fetch(FAILED_BLOCKS.BLOCK_NUMBER.notNull())
    }

    fun processFailedBlock(
        blockNumber: Long,
        jooqConfiguration: Configuration? = null,
    ) {
        val jooqConf = jooqConfiguration ?: configuration
        jooqConf.dsl().transaction { cfg ->
            cfg.dsl().insertInto(PROCESSED_BLOCKS)
                .set(PROCESSED_BLOCKS.BLOCK_NUMBER, blockNumber)
                .execute()

            cfg.dsl().delete(FAILED_BLOCKS)
                .where(FAILED_BLOCKS.BLOCK_NUMBER.eq(blockNumber))
                .execute()
        }
    }
}
