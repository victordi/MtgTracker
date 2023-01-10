package com.mtg.tracker.data.query

import arrow.core.Either
import arrow.core.leftIfNull
import com.mtg.tracker.data.PointSystem
import com.mtg.tracker.data.PointsSystems
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.SeasonNotFound
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory

object PointsSystemQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun registerSystem(seasonId: Int, pointSystem: PointSystem): Either<Failure, Int> = safeTransaction {
        require(pointSystem.placeScore.size == 4) { "placeScore should be of size 4" }
        PointsSystems.insertAndGetId {
            it[PointsSystems.seasonId] = seasonId
            it[firstPlace] = pointSystem.placeScore[0]
            it[secondPlace] = pointSystem.placeScore[1]
            it[thirdPlace] = pointSystem.placeScore[2]
            it[fourthPlace] = pointSystem.placeScore[3]
            it[kill] = pointSystem.kill
            it[commanderKill] = pointSystem.commanderKill
            it[infinite] = pointSystem.infinite
            it[bodyGuard] = pointSystem.bodyGuard
        }.value
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }

    suspend fun find(seasonId: Int): Either<Failure, PointSystem> = safeTransaction {
        PointsSystems.select(PointsSystems.seasonId eq seasonId)
            .map {
                PointSystem(
                    listOf(
                        it[PointsSystems.firstPlace],
                        it[PointsSystems.secondPlace],
                        it[PointsSystems.thirdPlace],
                        it[PointsSystems.fourthPlace]
                    ),
                    it[PointsSystems.kill],
                    it[PointsSystems.commanderKill],
                    it[PointsSystems.infinite],
                    it[PointsSystems.bodyGuard]
                )
            }
            .firstOrNull()
    }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { SeasonNotFound.also { logger.error(it.message) } }
}
