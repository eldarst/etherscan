package com.etherscan.app.service.impl

import com.etherscan.app.model.BalanceChangeResultInfo
import com.etherscan.app.repository.BlockProcessRepository
import com.etherscan.app.repository.TransactionRepository
import com.etherscan.app.service.StatsService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class StatsServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val blockProcessRepository: BlockProcessRepository,
) : StatsService {
    override fun getMaxBalanceChange(): BalanceChangeResultInfo {
        val lastProcessedBlocks = blockProcessRepository.findNLastProcessedBlocks(100)
        if (lastProcessedBlocks.isEmpty()) {
            logger.warn { "Doesn't have any saved blocks in the system" }
            error("Couldn't find any transactions")
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

        val (address, netChange) = balanceChanges.maxByOrNull { it.value.abs() } ?: error("Couldn't count max balance change address")
        return BalanceChangeResultInfo(address, netChange.toBigInteger())
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
