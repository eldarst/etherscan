package com.etherscan.app

import com.github.springtestdbunit.DbUnitTestExecutionListener
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.transaction.TransactionalTestExecutionListener

@TestExecutionListeners(
    listeners = [
        DependencyInjectionTestExecutionListener::class,
        TransactionalTestExecutionListener::class,
        DbUnitTestExecutionListener::class,
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
annotation class BaseDbTest
