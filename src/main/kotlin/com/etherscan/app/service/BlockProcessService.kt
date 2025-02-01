package com.etherscan.app.service

import com.etherscan.app.model.TransactionEntity
import com.etherscan.app.repository.BlockProcessRepository
import com.etherscan.app.repository.TransactionRepository
import org.jooq.Configuration
import org.springframework.stereotype.Service

@Service
class BlockProcessService(
    private val transactionRepository: TransactionRepository,
    private val blockProcessRepository: BlockProcessRepository,
    private val jooqConfiguration: Configuration,
) {
    fun processBlockTransactions(
        blockTransactions: Collection<TransactionEntity>,
        blockNumber: Long,
    ) {
        jooqConfiguration.dsl().transaction { cfg ->
            transactionRepository.insertTransactions(blockTransactions, cfg)
            blockProcessRepository.insertProcessedBlock(blockNumber, cfg)
        }
    }

    fun processFailedBlockTransactions(
        blockTransactions: Collection<TransactionEntity>,
        blockNumber: Long,
    ) {
        jooqConfiguration.dsl().transaction { cfg ->
            transactionRepository.insertTransactions(blockTransactions, cfg)
            blockProcessRepository.processFailedBlock(blockNumber, cfg)
        }
    }

    fun addFailedBlock(blockNumber: Long) = blockProcessRepository.insertFailedBlock(blockNumber)

    fun findLastProcessedBlock() = blockProcessRepository.findNLastProcessedBlocks()

    fun findAllFailedBlock() = blockProcessRepository.findAllFailedBlock()
}
