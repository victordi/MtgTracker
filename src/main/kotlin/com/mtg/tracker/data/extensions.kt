package com.mtg.tracker.data

import arrow.core.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun <T> safeTransaction(block: Transaction.() -> T): Either<Throwable, T> = withContext(Dispatchers.IO) {
    Either
        .catch { transaction { block() } }
}