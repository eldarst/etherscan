package com.etherscan.app.controller

import com.etherscan.app.model.BalanceChangeResultInfo
import com.etherscan.app.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatsController(
    private val statsService: StatsService,
) {
    @GetMapping("/max-balance-change")
    fun getMaxBalanceChange(): ResponseEntity<BalanceChangeResultInfo> {
        val maxBalanceChange = statsService.getMaxBalanceChange()
        return ResponseEntity.ok(maxBalanceChange)
    }
}
