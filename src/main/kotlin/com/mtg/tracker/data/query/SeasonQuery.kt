package com.mtg.tracker.data.query

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.leftIfNull
import com.mtg.tracker.data.NewSeasonRequest
import com.mtg.tracker.data.PointsSystems
import com.mtg.tracker.data.Season
import com.mtg.tracker.data.SeasonStats
import com.mtg.tracker.data.Seasons
import com.mtg.tracker.data.calculateAverage
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.PlayerNotFound
import com.mtg.tracker.failure.SeasonNotFound
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

object SeasonQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun insert(player1: String, player2: String, player3: String, player4: String) = safeTransaction {
        Seasons.insertAndGetId {
            it[Seasons.player1] = player1
            it[points1] = 0
            it[Seasons.player2] = player2
            it[points2] = 0
            it[Seasons.player3] = player3
            it[points3] = 0
            it[Seasons.player4] = player4
            it[points4] = 0
        }.value
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun startSeason(season: NewSeasonRequest): Either<Failure, Int> = either {
        ensure(season.player1 != season.player2
                && season.player1 != season.player3
                && season.player1 != season.player4
                && season.player2 != season.player3
        ) {
            PlayerNotFound
        }
        val seasonId = insert(season.player1, season.player2, season.player3, season.player4).bind()
        PointsSystemQuery.registerSystem(seasonId, season.pointSystem).bind()
        seasonId
    }

    suspend fun find(id: Int): Either<Failure, Season> = safeTransaction {
        Seasons.select(Seasons.id eq id)
            .map {
                val players = listOf(
                    it[Seasons.player1] to it[Seasons.points1],
                    it[Seasons.player2] to it[Seasons.points2],
                    it[Seasons.player3] to it[Seasons.points3],
                    it[Seasons.player4] to it[Seasons.points4]
                )
                Season(it[Seasons.id].value, players)
            }
            .firstOrNull()
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { SeasonNotFound.also { logger.error(it.message) } }

    suspend fun delete(id: Int): Either<Failure, Int> = safeTransaction {
        PointsSystems.deleteWhere { seasonId eq id }
        Seasons.deleteWhere { Seasons.id eq id }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun find(): Either<Failure, List<Season>> = safeTransaction {
        Seasons.selectAll()
            .map {
                val players = listOf(
                    it[Seasons.player1] to it[Seasons.points1],
                    it[Seasons.player2] to it[Seasons.points2],
                    it[Seasons.player3] to it[Seasons.points3],
                    it[Seasons.player4] to it[Seasons.points4]
                )
                Season(it[Seasons.id].value, players)
            }
            .sortedBy { it.id }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun updatePoints(season: Season, playerName: String, points: Int) = safeTransaction {
        val col = when(season.players.indexOfFirst { it.first == playerName }) {
            0 -> Seasons.points1
            1 -> Seasons.points2
            2 -> Seasons.points3
            3 -> Seasons.points4
            else -> throw IllegalArgumentException("Player $playerName is not part of this season")
        }
        Seasons.update({Seasons.id eq season.id}) {
            it[col] = points
        }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun stats(seasonId: Int): Either<Failure, List<Pair<String, SeasonStats>>> = either {
        val season = find(seasonId).bind()
        season.players.map { it.first }.associateWith {
            val deckStats = gameResultsPerSeason(seasonId, it).bind()
            val playerStats = deckStats.map { deck -> deck.stats }.calculateAverage()
            SeasonStats(playerStats, deckStats)
        }.toList()
    }
}
