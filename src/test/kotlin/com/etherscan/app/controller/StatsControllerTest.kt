package com.etherscan.app.controller

import com.etherscan.app.BaseDbTest
import com.etherscan.app.exception.exception.ErrorResponse
import com.etherscan.app.model.BalanceChangeResultInfo
import com.github.springtestdbunit.annotation.DatabaseOperation
import com.github.springtestdbunit.annotation.DatabaseSetup
import com.github.springtestdbunit.annotation.DatabaseTearDown
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.math.BigInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@BaseDbTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class StatsControllerTest(
    @Autowired val webTestClient: WebTestClient,
) {
    @Test
    @DatabaseTearDown(value = ["/db/controller/processed_blocks_no_transactions.xml"], type = DatabaseOperation.TRUNCATE_TABLE)
    @DatabaseSetup(value = ["/db/controller/processed_blocks_no_transactions.xml"], type = DatabaseOperation.CLEAN_INSERT)
    fun `When return error when has processed blocks but no transactions`() {
        val expectedError =
            ErrorResponse(
                error = "Resource wasn't found",
                message = "Couldn't find any transactions",
            )
        webTestClient.get().uri("/max-balance-change")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<ErrorResponse>()
            .isEqualTo(expectedError)
    }

    @Test
    @DatabaseTearDown(value = ["/db/controller/processed_blocks_and_transactions.xml"], type = DatabaseOperation.TRUNCATE_TABLE)
    @DatabaseSetup(value = ["/db/controller/processed_blocks_and_transactions.xml"], type = DatabaseOperation.CLEAN_INSERT)
    fun `When request max balance should return balance`() {
        val expectedMaxBalanceChange = BalanceChangeResultInfo(address = "0x111", netChange = BigInteger("-351412148"))

        webTestClient.get().uri("/max-balance-change")
            .exchange()
            .expectStatus().isOk
            .expectBody<BalanceChangeResultInfo>()
            .isEqualTo(expectedMaxBalanceChange)
    }

    @Test
    fun `When return error when no block were processed`() {
        val expectedError =
            ErrorResponse(
                error = "Resource wasn't found",
                message = "Couldn't find any processed blocks",
            )
        webTestClient.get().uri("/max-balance-change")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<ErrorResponse>()
            .isEqualTo(expectedError)
    }
}
