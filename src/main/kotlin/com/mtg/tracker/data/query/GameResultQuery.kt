package com.mtg.tracker.data.query

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.leftIfNull
import com.mtg.tracker.controller.NewGameResultReq
import com.mtg.tracker.data.GameResult
import com.mtg.tracker.data.GameResults
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.DeckNotFound
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.GameNotInSeason
import com.mtg.tracker.failure.PlayerNotInSeason
import com.mtg.tracker.failure.SeasonNotFound
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory

object GameResultQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun findAll(id: Int): Either<Failure, List<GameResult>> = safeTransaction {
        GameResults.select { GameResults.seasonId eq id }
            .map {
                GameResult(
                    it[GameResults.id].value,
                    it[GameResults.playerName],
                    it[GameResults.deckName],
                    it[GameResults.place],
                    it[GameResults.startOrder],
                    it[GameResults.kills],
                    it[GameResults.commanderKills],
                    it[GameResults.infinite],
                    it[GameResults.bodyGuard],
                    it[GameResults.penalty]
                )
            }
            .sortedBy { it.playerName }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    private suspend fun insert(id: Int, game: NewGameResultReq, pointsMade: Int): Either<Failure, Int> = safeTransaction {
        GameResults.insertAndGetId {
            it[seasonId] = id
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

    suspend fun reportGame(seasonId: Int, game: NewGameResultReq): Either<Failure, Int> = either {
        val season = SeasonQuery.find(seasonId).bind()
        val prevPoints = Either.Right(season.players.find { it.first == game.playerName })
            .leftIfNull { PlayerNotInSeason.also { logger.error(it.message) } }
            .bind()
            .second
        val player = PlayerQuery.findByName(game.playerName).bind()
        val deck = Either.Right(player.decks.find { it.name == game.deckName })
            .leftIfNull { DeckNotFound.also { logger.error(it.message) } }
            .bind()
        val pointSystem = PointsSystemQuery.find(seasonId).bind()
        val pointsMade = game.points(pointSystem, deck.tier)
        SeasonQuery.updatePoints(season, game.playerName, prevPoints + pointsMade).bind()
        insert(seasonId, game, pointsMade).bind()
    }

    suspend fun deleteGame(seasonId: Int, id: Int): Either<Failure, Int> =
        findAll(seasonId)
            .map { it.find { game -> game.id == id } }
            .leftIfNull { GameNotInSeason }
            .flatMap {
                safeTransaction {
                    GameResults.deleteWhere { GameResults.id eq id }
                }
                    .tapLeft { logger.error(it.message) }
                    .mapLeft { DatabaseFailure }
            }
}