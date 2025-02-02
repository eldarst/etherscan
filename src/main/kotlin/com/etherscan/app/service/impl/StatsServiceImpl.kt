package com.etherscan.app.service.impl

import com.etherscan.app.exception.exception.NoRecordsFoundException
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
            throw NoRecordsFoundException("Couldn't find any processed blocks")
        }
        logger.info { "Calculating max balance change of blocks ${lastProcessedBlocks.last()}...${lastProcessedBlocks.first()}" }
        val lastProcessedTransactions = transactionRepository.getByBlockNumbers(lastProcessedBlocks)
        if (lastProcessedTransactions.isEmpty()) {
            logger.warn { "Doesn't have any saved blocks in the system" }
            throw NoRecordsFoundException("Couldn't find any transactions")
        }
        logger.info { "There are ${lastProcessedTransactions.size} transaction in this blocks" }

        val balanceChanges = hashMapOf<String, BigDecimal>()
        for (transaction in lastProcessedTransactions) {
            balanceChanges.merge(
                transaction.fromAddress,
                -transaction.value,
            ) { oldValue, decreaseValue -> oldValue + decreaseValue }

            if (transaction.toAddress != null) {
                balanceChanges.merge(
                    transaction.toAddress,
                    transaction.value,
                ) { oldValue, increaseValue -> oldValue + increaseValue }
            }
        }

        val (address, netChange) = balanceChanges.maxBy { it.value.abs() }
        return BalanceChangeResultInfo(address, netChange.toBigInteger())
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
