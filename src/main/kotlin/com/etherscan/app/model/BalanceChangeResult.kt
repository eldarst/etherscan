package com.etherscan.app.model

import java.math.BigInteger

data class BalanceChangeResult(
    val address: String,
    val netChange: BigInteger,
)
