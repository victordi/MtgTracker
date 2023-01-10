package com.mtg.tracker.data.query

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.leftIfNull
import com.mtg.tracker.data.Deck
import com.mtg.tracker.data.DeckStats
import com.mtg.tracker.data.Decks
import com.mtg.tracker.data.GameResult
import com.mtg.tracker.data.GameResults
import com.mtg.tracker.data.Player
import com.mtg.tracker.data.PlayerStats
import com.mtg.tracker.data.Players
import com.mtg.tracker.data.Seasons
import com.mtg.tracker.data.Stats
import com.mtg.tracker.data.calculateAverage
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.NameNotUniqueFailure
import com.mtg.tracker.failure.PlayerNotFound
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory

object PlayerQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun failIfExists(name: String): Either<Failure, Unit> = safeTransaction {
        if (Players.selectAll().map { it[Players.name] }.contains(name)) null else Unit
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { NameNotUniqueFailure.also { logger.error(it.message) } }

    suspend fun insert(name: String): Either<Failure, String> = failIfExists(name).flatMap {
        safeTransaction {
            name.apply { Players.insert { it[Players.name] = name } }
        }
            .tapLeft { logger.error(it.message) }
            .mapLeft { DatabaseFailure }
    }

    suspend fun findByName(name: String): Either<Failure, Player> = safeTransaction {
        val playerExists = Players.selectAll().map { it[Players.name] }.contains(name)
        if (!playerExists) null
        else Decks
            .select { Op.build { Decks.playerName eq name } }
            .map { Deck(it[Decks.name], it[Decks.tier]) }
            .let { Player(name, it) }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { PlayerNotFound.also { logger.error(it.message) } }

    suspend fun findAll(): Either<Failure, List<Player>> = safeTransaction {
        Players.leftJoin(Decks)
            .selectAll()
            .groupBy { it[Players.name] }
            .map { (name, decks) -> Player(
                    name, decks.filter { it.getOrNull(Decks.name) != null }.map { Deck(it[Decks.name], it[Decks.tier]) }
                )
            }
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun delete(name: String): Either<Failure, Unit> = safeTransaction {
        val deckCount = Decks.deleteWhere { playerName eq name }
        val playerCount = Players.deleteWhere { Players.name eq name }
        if (deckCount + playerCount == 0) null
        else Unit
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { PlayerNotFound.also { logger.error(it.message) } }

    suspend fun stats(name: String): Either<Failure, PlayerStats> = either {
        val seasons = SeasonQuery.find().bind()
            .filter { it.players.map { player -> player.first }.contains(name) }
            .map { it.id }
        val deckStatsPerSeason = seasons
            .associateWith { gameResultsPerSeason(it, name).bind() }
            .filter { it.value.isNotEmpty() }
        val avgDeckStats = deckStatsPerSeason
            .values
            .flatten()
            .groupBy { it.deckName }
            .mapValues { (deckName, deckStats) ->
                DeckStats(deckName, deckStats.map { it.stats }.calculateAverage())
            }
            .values.toList()

        val avgStats = avgDeckStats.map { it.stats }.calculateAverage()
        val statsPerSeason = deckStatsPerSeason.mapValues { (_, deckStats) ->
            deckStats.map { it.stats }.calculateAverage()
        }

        PlayerStats(name, avgDeckStats, avgStats, deckStatsPerSeason, statsPerSeason)
    }

    private suspend fun gameResultsPerSeason(seasonId: Int, playerName: String) = safeTransaction {
        GameResults
            .select { GameResults.seasonId eq seasonId and (GameResults.playerName eq playerName) }
            .groupBy { it[GameResults.deckName] }
            .mapValues { (deckName, resultRows) ->
                val gameResults = resultRows.map {
                    GameResult(
                        it[GameResults.seasonId], it[GameResults.playerName], it[GameResults.deckName],
                        it[GameResults.place], it[GameResults.startOrder], it[GameResults.kills],
                        it[GameResults.commanderKills], it[GameResults.infinite], it[GameResults.bodyGuard],
                        it[GameResults.penalty]
                    )
                }
                val stats = Stats(
                    gameResults.size,
                    gameResults.filter { it.place == 1 }.size,
                    gameResults.filter { it.place == 1 && it.startOrder == 1 }.size,
                    gameResults.filter { it.place == 1 && it.startOrder == 2 }.size,
                    gameResults.filter { it.place == 1 && it.startOrder == 3 }.size,
                    gameResults.filter { it.place == 1 && it.startOrder == 4 }.size,
                    gameResults.filter { it.place == 1 && it.infinite }.size,
                    gameResults.sumOf { it.place }.toDouble() / gameResults.size.toDouble(),
                    gameResults.sumOf { it.kills }.toDouble() / gameResults.size.toDouble(),
                    gameResults.sumOf { it.commanderKills }.toDouble() / gameResults.size.toDouble(),
                )
                DeckStats(deckName, stats)
            }
            .values
            .toList()
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
}