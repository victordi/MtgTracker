package com.mtg.tracker.data

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.leftIfNull
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.NameNotUniqueFailure
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory

object DeckQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun failIfExists(name: String): Either<Failure, Unit> = safeTransaction {
        if (Decks.selectAll().map { it[Decks.name] }.contains(name)) null else Unit
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { NameNotUniqueFailure }

    suspend fun insert(deck: Deck, player: String): Either<Failure, Deck> = failIfExists(deck.name).flatMap {
        safeTransaction {
            deck.apply {
                Decks.insert {
                    it[name] = deck.name
                    it[tier] = deck.tier
                    it[playerName] = player
                }
            }
        }
            .tapLeft { logger.error(it.message) }
            .mapLeft { DatabaseFailure }
    }

    suspend fun findAll(player: String): Either<Failure, List<Deck>> = safeTransaction {
        Decks.select { Decks.playerName eq player }
            .map { Deck(it[Decks.name], it[Decks.tier]) }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun findAll(): Either<Failure, List<Deck>> = safeTransaction {
        Decks.selectAll().map { Deck(it[Decks.name], it[Decks.tier]) }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
}