package com.mtg.tracker.data

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.leftIfNull
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.NameNotUniqueFailure
import com.mtg.tracker.failure.PlayerNotFound
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory

object PlayerQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun failIfExists(name: String): Either<Failure, Unit> = safeTransaction {
        if (Players.selectAll().map { it[Players.name] }.contains(name)) null else Unit
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { NameNotUniqueFailure }

    suspend fun insert(name: String): Either<Failure, String> = failIfExists(name).flatMap {
        safeTransaction {
            name.apply { Players.insert { it[Players.name] = name } }
        }
            .tapLeft { logger.error(it.message) }
            .mapLeft { DatabaseFailure }
    }

    suspend fun findByName(name: String): Either<Failure, Player> = safeTransaction {
        val decks = Decks
            .select { Op.build { Decks.playerName eq name } }
            .map { Deck(it[Decks.name], it[Decks.tier]) }
        if (decks.isEmpty()) null
        else Player(name, decks)
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { PlayerNotFound }

    suspend fun findAll(): Either<Failure, List<Player>> = safeTransaction {
        Players.innerJoin(Decks)
            .selectAll()
            .groupBy { it[Players.name] }
            .map { (name, decks) -> Player(name, decks.map { Deck(it[Decks.name], it[Decks.tier]) }) }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
}