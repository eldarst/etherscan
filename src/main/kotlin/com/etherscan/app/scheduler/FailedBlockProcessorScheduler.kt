package com.etherscan.app.scheduler

import com.etherscan.app.service.BlockProcessService
import com.etherscan.app.service.EtherscanService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FailedBlockProcessorScheduler(
    private val etherscanService: EtherscanService,
    private val blockProcessService: BlockProcessService,
) {
    private val mutex = Mutex()

    @Scheduled(cron = "0 0/5 * * * *")
    @Async
    fun processFailedBocks() =
        runBlocking {
            if (mutex.isLocked) {
                logger.warn { "FailedBlockProcessorScheduler cron job is already running. Second job won't start in parallel" }
                return@runBlocking
            }
            mutex.withLock {
                val failedBlocks = blockProcessService.findAllFailedBlock()
                logger.info { "Failed blocks count: ${failedBlocks.size}" }

                for (blockNumber in failedBlocks) {
                    val blockTransactions = etherscanService.getBlockTransactions(blockNumber, longRunning = true)
                    if (blockTransactions == null) {
                        blockProcessService.addFailedBlock(blockNumber)
                        logger.info { "Block $blockNumber will be processed later" }
                        continue
                    }
                    blockProcessService.processFailedBlockTransactions(blockTransactions, blockNumber)
                }
            }
        }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
