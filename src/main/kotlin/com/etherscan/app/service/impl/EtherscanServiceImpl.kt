package com.etherscan.app.service.impl

import com.etherscan.app.model.TransactionEntity
import com.etherscan.app.model.client.BlockResponse
import com.etherscan.app.model.client.BlockResult
import com.etherscan.app.service.EtherscanApiService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class EtherscanServiceImpl(
    private val httpClient: HttpClient,
    @Value("\${etherscan.api.url}") private val apiUrl: String,
    @Value("\${etherscan.api.key}") private val apiKey: String,
) : EtherscanApiService {
    override suspend fun getBlockTransactions(
        blockNumber: Long,
        longRunning: Boolean,
    ): Collection<TransactionEntity>? {
        logger.info { "Processing a block: $blockNumber" }
        val blockResponse =
            runWithRetry("Get block info $blockNumber") {
                if (longRunning) {
                    getBlockByNumber(blockNumber, 300_000)
                } else {
                    getBlockByNumber(blockNumber)
                }
            }
        if (blockResponse == null) {
            logger.warn { "Couldn't get transactions info in a block: $blockNumber" }
            return null
        }
        return blockResponse.result.transactions.map { transaction ->
            val value = hexToBigDecimal(transaction.value)
            TransactionEntity(
                blockNumber = blockNumber,
                txHash = transaction.hash,
                fromAddress = transaction.from,
                toAddress = transaction.to,
                value = value,
            )
        }
    }

    private suspend fun getBlockByNumber(
        blockNumber: Long,
        timeout: Long = 30_000,
    ): BlockResponse<BlockResult> {
        logger.info { "Fetching transactions from block $blockNumber" }

        val tag = "0x" + blockNumber.toString(16)
        val response: HttpResponse =
            httpClient.get(apiUrl) {
                parameter("module", "proxy")
                parameter("action", "eth_getBlockByNumber")
                parameter("tag", tag)
                parameter("boolean", true)
                parameter("apikey", apiKey)
                timeout {
                    requestTimeoutMillis = timeout
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = timeout
                }
            }

        val blockResponse: BlockResponse<BlockResult> = response.body()
        return blockResponse
    }

    override suspend fun getLatestBlockNumber(): Long? =
        try {
            val response: HttpResponse =
                httpClient.get(apiUrl) {
                    parameter("module", "proxy")
                    parameter("action", "eth_blockNumber")
                    parameter("apikey", apiKey)
                }

            val blockResponse: BlockResponse<String> = response.body()
            hexToLong(blockResponse.result)
        } catch (e: Exception) {
            logger.warn { "Couldn't get latest block number. Error: ${e.message}" }
            null
        }

    private suspend fun <T> runWithRetry(
        requestInfo: String,
        maxRetries: Int = 3,
        delayBetweenRetriesMillis: Long = 1000,
        block: suspend () -> T,
    ): T? {
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                logger.warn { "Attempt ${attempt + 1} failed when making request: $requestInfo. Error: ${e.message}" }
                if (attempt < maxRetries - 1) {
                    delay(delayBetweenRetriesMillis)
                }
            }
        }
        return null
    }

    private fun hexToLong(hex: String): Long = hex.removePrefix("0x").toLong(16)

    private fun hexToBigDecimal(hex: String): BigDecimal = BigDecimal(BigInteger(hex.removePrefix("0x"), 16))

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
