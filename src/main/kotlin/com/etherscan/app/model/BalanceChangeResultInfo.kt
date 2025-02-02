package com.etherscan.app.model

import java.math.BigInteger

data class BalanceChangeResultInfo(
    val address: String,
    val netChange: BigInteger,
)
