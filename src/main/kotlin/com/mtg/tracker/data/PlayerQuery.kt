package com.mtg.tracker.data

import arrow.core.Either
import com.mtg.tracker.failure.Failure
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

object PlayerQuery {
    suspend fun insert(name: String): Either<Failure, String> = safeTransaction {
        name.apply { Players.insert { it[Players.name] = name } }
    }

    suspend fun findByName(name: String): Either<Failure, Player> = safeTransaction {
        val decks = Decks
            .select { Op.build { Decks.playerName eq name } }
            .map { Deck(it[Decks.name], it[Decks.tier]) }
        Player(name, decks)
    }

    suspend fun findAll(): Either<Failure, List<Player>> = safeTransaction {
        Players.innerJoin(Decks)
            .selectAll()
            .groupBy { it[Players.name] }
            .map { (name, decks) -> Player(name, decks.map { Deck(it[Decks.name], it[Decks.tier]) }) }
    }
}