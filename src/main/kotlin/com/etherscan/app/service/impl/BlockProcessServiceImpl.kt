package com.etherscan.app.service.impl

import com.etherscan.app.model.TransactionEntity
import com.etherscan.app.repository.BlockProcessRepository
import com.etherscan.app.repository.TransactionRepository
import com.etherscan.app.service.BlockProcessingService
import org.jooq.Configuration
import org.springframework.stereotype.Service

@Service
class BlockProcessServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val blockProcessRepository: BlockProcessRepository,
    private val jooqConfiguration: Configuration,
) : BlockProcessingService {
    override fun processBlockTransactions(
        blockTransactions: Collection<TransactionEntity>,
        blockNumber: Long,
    ) {
        jooqConfiguration.dsl().transaction { cfg ->
            transactionRepository.insertTransactions(blockTransactions, cfg)
            blockProcessRepository.insertProcessedBlock(blockNumber, cfg)
        }
    }

    override fun processFailedBlockTransactions(
        blockTransactions: Collection<TransactionEntity>,
        blockNumber: Long,
    ) {
        jooqConfiguration.dsl().transaction { cfg ->
            transactionRepository.insertTransactions(blockTransactions, cfg)
            blockProcessRepository.processFailedBlock(blockNumber, cfg)
        }
    }

    override fun addFailedBlock(blockNumber: Long) = blockProcessRepository.insertFailedBlock(blockNumber)

    override fun findLastProcessedBlock() = blockProcessRepository.findNLastProcessedBlocks().firstOrNull()

    override fun findAllFailedBlock() = blockProcessRepository.findAllFailedBlock()
}
