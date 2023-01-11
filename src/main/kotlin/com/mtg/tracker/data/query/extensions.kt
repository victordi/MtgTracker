package com.mtg.tracker.data.query

import arrow.core.Either
import com.mtg.tracker.data.DeckStats
import com.mtg.tracker.data.GameResult
import com.mtg.tracker.data.GameResults
import com.mtg.tracker.data.Stats
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("QueryExtensions")

suspend fun gameResultsPerSeason(seasonId: Int, player: String): Either<Failure, List<DeckStats>> = safeTransaction {
    GameResults
        .select { GameResults.seasonId eq seasonId and (GameResults.playerName eq player) }
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