package com.mtg.tracker.data.query

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.leftIfNull
import arrow.core.right
import com.mtg.tracker.data.GameResult
import com.mtg.tracker.data.GameResults
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.DeckNotFound
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.PlayerNotFound
import com.mtg.tracker.failure.PlayerNotInSeason
import org.jetbrains.exposed.sql.insertAndGetId
import org.slf4j.LoggerFactory

object GameResultQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun insert(game: GameResult, pointsMade: Int): Either<Failure, Int> = safeTransaction {
        GameResults.insertAndGetId {
            it[seasonId] = game.seasonId
            it[playerName] = game.playerName
            it[deckName] = game.deckName
            it[place] = game.place
            it[startOrder] = game.startOrder
            it[kills] = game.kills
            it[commanderKills] = game.commanderKills
            it[infinite] = game.infinite
            it[bodyGuard] = game.bodyGuard
            it[penalty] = game.penalty
            it[points] = pointsMade
        }.value
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun reportGame(game: GameResult): Either<Failure, Int> = either {
        val season = SeasonQuery.find(game.seasonId).bind()
        val prevPoints = Either.Right(season.players.find { it.first == game.playerName })
            .leftIfNull { PlayerNotInSeason.also { logger.error(it.message) } }
            .bind()
            .second
        val player = PlayerQuery.findByName(game.playerName).bind()
        val deck = Either.Right(player.decks.find { it.name == game.deckName })
            .leftIfNull { DeckNotFound.also { logger.error(it.message) } }
            .bind()
        val pointSystem = PointsSystemQuery.find(game.seasonId).bind()
        val pointsMade = game.points(pointSystem, deck.tier)
        SeasonQuery.updatePoints(season, game.playerName, prevPoints + pointsMade).bind()
        insert(game, pointsMade).bind()
    }
}