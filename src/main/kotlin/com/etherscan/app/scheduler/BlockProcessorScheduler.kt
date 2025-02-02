package com.etherscan.app.scheduler

import com.etherscan.app.service.BlockProcessService
import com.etherscan.app.service.EtherscanService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BlockProcessorScheduler(
    private val etherscanService: EtherscanService,
    private val blockProcessService: BlockProcessService,
    @Value("\${scheduler.shouldRunLatestFirst}") private val shouldRunLatestFirst: Boolean,
) {
    private val mutex = Mutex()

    @Scheduled(cron = "0 * * * * *")
    @Async
    fun processNewBlocks() =
        runBlocking {
            if (mutex.isLocked && !shouldRunLatestFirst) {
                logger.warn { "BlockProcessorScheduler cron job is already running. Second job won't start in parallel" }
                return@runBlocking
            }
            if (shouldRunLatestFirst) {
                logger.info { "Will process latest records first down to $START_BLOCK" }
                processFromLastUpdated()
            } else {
                logger.info { "Will process starting from $START_BLOCK up to latest. Jobs won't run in parallel" }
                mutex.withLock { processFromLastUpdated() }
            }
        }

    private suspend fun processFromLastUpdated() {
        val lastProcessedBlock = blockProcessService.findLastProcessedBlock() ?: (START_BLOCK - 1)
        logger.info { "The last processed block: $lastProcessedBlock" }

        val latestBlockHex = etherscanService.getLatestBlockNumber()
        if (latestBlockHex == null) {
            logger.warn { "Couldn't get the last block" }
            return
        }
        val latestBlock = etherscanService.hexToLong(latestBlockHex.result)
        logger.info { "The latest block: $latestBlock" }

        val blocks = if (shouldRunLatestFirst) {
            latestBlock downTo (lastProcessedBlock + 1)
        } else {
            (lastProcessedBlock + 1)..latestBlock
        }
        for (blockNumber in blocks) {
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
