package com.etherscan.app.controller

import com.etherscan.app.model.BalanceChangeResult
import com.etherscan.app.service.StatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatsController(
    private val statsService: StatsService,
) {
    @GetMapping("/max-balance-change")
    fun getMaxBalanceChange(): BalanceChangeResult? {
        val maxBalanceChange = statsService.getMaxBalanceChange()
        return maxBalanceChange
    }
}
