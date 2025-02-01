package com.etherscan.app.scheduler

import com.etherscan.app.service.BlockProcessService
import com.etherscan.app.service.EtherscanService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BlockProcessorScheduler(
    private val etherscanService: EtherscanService,
    private val blockProcessService: BlockProcessService,
) {
    @Scheduled(cron = "0 * * * * *")
    @Async
    fun processNewBlocks() =
        runBlocking {
            val lastProcessedBlock = blockProcessService.findLastProcessedBlock().firstOrNull() ?: (START_BLOCK - 1)
            logger.info { "The last processed block: $lastProcessedBlock" }

            val latestBlockHex = etherscanService.getLatestBlockNumber()
            if (latestBlockHex == null) {
                logger.warn { "Couldn't get the last block" }
                return@runBlocking
            }
            val latestBlock = etherscanService.hexToLong(latestBlockHex.result)
            logger.info { "The latest block: $latestBlock" }

            for (blockNumber in latestBlock downTo (lastProcessedBlock + 1)) {
                val blockTransactions = etherscanService.getBlockTransactions(blockNumber)
                if (blockTransactions == null) {
                    blockProcessService.addFailedBlock(blockNumber)
                    continue
                }
                blockProcessService.processBlockTransactions(blockTransactions, blockNumber)
            }
        }

    companion object {
        private const val START_BLOCK = 19_000_000L
        private val logger = KotlinLogging.logger { }
    }
}
