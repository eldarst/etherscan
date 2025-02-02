package com.etherscan.app.repository

import org.jooq.Configuration

interface BlockProcessRepository {
    fun insertProcessedBlock(
        blockNumber: Long,
        jooqConfiguration: Configuration? = null,
    )

    fun findNLastProcessedBlocks(n: Long = 1): Collection<Long>

    fun insertFailedBlock(blockNumber: Long)

    fun findAllFailedBlock(): List<Long>

    fun processFailedBlock(
        blockNumber: Long,
        jooqConfiguration: Configuration? = null,
    )
}
