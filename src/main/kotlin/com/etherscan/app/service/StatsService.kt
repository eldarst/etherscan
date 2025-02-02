package com.etherscan.app.service

import com.etherscan.app.model.BalanceChangeResult
import com.etherscan.app.repository.BlockProcessRepository
import com.etherscan.app.repository.TransactionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class StatsService(
    private val transactionRepository: TransactionRepository,
    private val blockProcessRepository: BlockProcessRepository,
) {
    fun getMaxBalanceChange(): BalanceChangeResult? {
        val lastProcessedBlocks = blockProcessRepository.findNProcessedBlocks(100)
        if (lastProcessedBlocks.isEmpty()) {
            logger.warn { "Doesn't have any saved blocks in the system" }
            return null
        }
        logger.info { "Calculating max balance change of blocks ${lastProcessedBlocks.last()}...${lastProcessedBlocks.first()}" }
        val lastProcessedTransactions = transactionRepository.getByBlockNumbers(lastProcessedBlocks)

        val balanceChanges = hashMapOf<String, BigDecimal>()
        for (transaction in lastProcessedTransactions) {
            balanceChanges.merge(
                transaction.fromAddress,
                BigDecimal.ZERO,
            ) { oldValue, decreaseValue -> oldValue - decreaseValue }

            if (transaction.toAddress != null) {
                balanceChanges.merge(
                    transaction.toAddress,
                    BigDecimal.ZERO,
                ) { oldValue, increaseValue -> oldValue + increaseValue }
            }
        }

        val (address, netChange) = balanceChanges.maxByOrNull { it.value.abs() } ?: return null
        return BalanceChangeResult(address, netChange.toBigInteger())
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
