package com.etherscan.app.scheduler

import com.etherscan.app.service.BlockProcessingService
import com.etherscan.app.service.EtherscanApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val etherscanService: EtherscanApiService,
    private val blockProcessService: BlockProcessingService,
    @Value("\${scheduler.shouldRunLatestFirst}") private val shouldRunLatestFirst: Boolean,
    @Value("\${scheduler.parallelApiCalls}") private val parallelApiCalls: Long,
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

        val latestBlock = etherscanService.getLatestBlockNumber()
        if (latestBlock == null) {
            logger.warn { "Couldn't get the last block" }
            return
        }
        logger.info { "The latest block: $latestBlock" }

        val blocks =
            if (shouldRunLatestFirst) {
                latestBlock downTo (lastProcessedBlock + 1)
            } else {
                (lastProcessedBlock + 1)..latestBlock
            }
        coroutineScope {
            for (startingBlock in blocks.step(parallelApiCalls)) {
                (0 until parallelApiCalls).map { parallelId ->
                    async {
                        val currentBlockId = startingBlock + parallelId
                        val blockTransactions = etherscanService.getBlockTransactions(currentBlockId)
                        if (blockTransactions == null) {
                            blockProcessService.addFailedBlock(currentBlockId)
                            logger.warn { "Block $currentBlockId wasn't read from API. Will be processed later" }
                            return@async
                        }
                        blockProcessService.processBlockTransactions(blockTransactions, currentBlockId)
                        logger.info { "Block $currentBlockId successfully processed and saved to system" }
                    }
                }.awaitAll()
            }
        }
    }

    companion object {
        private const val START_BLOCK = 19_000_000L
        private val logger = KotlinLogging.logger { }
    }
}
