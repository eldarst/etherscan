package com.etherscan.app.repository.impl

import com.etherscan.app.extensions.notNull
import com.etherscan.app.repository.BlockProcessRepository
import com.example.generated.jooq.tables.FailedBlocks.Companion.FAILED_BLOCKS
import com.example.generated.jooq.tables.ProcessedBlocks.Companion.PROCESSED_BLOCKS
import org.jooq.Configuration
import org.springframework.stereotype.Repository

@Repository
class BlockProcessRepositoryImpl(private val configuration: Configuration) : BlockProcessRepository {
    override fun insertProcessedBlock(
        blockNumber: Long,
        jooqConfiguration: Configuration?,
    ) {
        val jooqConf = jooqConfiguration ?: configuration

        jooqConf.dsl().insertInto(PROCESSED_BLOCKS)
            .set(PROCESSED_BLOCKS.BLOCK_NUMBER, blockNumber)
            .onConflict(PROCESSED_BLOCKS.BLOCK_NUMBER)
            .doNothing()
            .execute()
    }

    override fun findNLastProcessedBlocks(n: Long): Collection<Long> {
        return configuration.dsl().select(PROCESSED_BLOCKS.BLOCK_NUMBER)
            .from(PROCESSED_BLOCKS)
            .orderBy(PROCESSED_BLOCKS.BLOCK_NUMBER.desc())
            .limit(n)
            .fetch(PROCESSED_BLOCKS.BLOCK_NUMBER.notNull())
    }

    override fun insertFailedBlock(blockNumber: Long) {
        configuration.dsl().insertInto(FAILED_BLOCKS)
            .set(FAILED_BLOCKS.BLOCK_NUMBER, blockNumber)
            .onConflict(FAILED_BLOCKS.BLOCK_NUMBER)
            .doNothing()
            .execute()
    }

    override fun findAllFailedBlock(): List<Long> {
        return configuration.dsl().select(FAILED_BLOCKS.BLOCK_NUMBER)
            .from(FAILED_BLOCKS)
            .fetch(FAILED_BLOCKS.BLOCK_NUMBER.notNull())
    }

    override fun processFailedBlock(
        blockNumber: Long,
        jooqConfiguration: Configuration?,
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
