package com.mtg.tracker.data

import arrow.core.Either
import com.mtg.tracker.failure.Failure
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

object DeckQuery {
    suspend fun insert(deck: Deck, player: String): Either<Failure, Deck> = safeTransaction {
        deck.apply {
            Decks.insert {
                it[name] = deck.name
                it[tier] = deck.tier
                it[playerName] = player
            }
        }
    }

    suspend fun findAll(player: String): Either<Failure, List<Deck>> = safeTransaction {
        Decks.select { Decks.playerName eq player }
            .map { Deck(it[Decks.name], it[Decks.tier]) }
    }

    suspend fun findAll(): Either<Failure, List<Deck>> = safeTransaction {
        Decks.selectAll().map { Deck(it[Decks.name], it[Decks.tier]) }
    }
}