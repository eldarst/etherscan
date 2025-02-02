package com.etherscan.app.service

import com.etherscan.app.model.BalanceChangeResultInfo

interface StatsService {
    fun getMaxBalanceChange(): BalanceChangeResultInfo
}
