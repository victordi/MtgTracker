package com.mtg.tracker.data

import arrow.core.Either
import com.mtg.tracker.failure.DatabaseFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("DataQuery")

suspend fun <T> safeTransaction(block: Transaction.() -> T): Either<DatabaseFailure, T> = withContext(Dispatchers.IO) {
    Either
        .catch { transaction { block() } }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
}