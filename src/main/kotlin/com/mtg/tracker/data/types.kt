package com.mtg.tracker.data

import java.time.Instant

data class User(val username: String, val password: String)

data class SignedJWT(
    val iss: String,
    val jti: String,
    val iat: String,
    val exp: Int,
    val sub: String
)

val SignedJWT.isNotExpired: Boolean
    get() = exp > Instant.now().epochSecond

enum class Tier(val multiplier: Double) {
    I(1.0), II(1.5), III(2.0)
}

data class Player(val name: String, val decks: List<Deck>)

data class Deck(val name: String, val tier: Tier)

data class GameResult(
    val seasonId: Int,
    val playerName: String,
    val deckName: String,
    val place: Int,
    val startOrder: Int,
    val kills: Int,
    val commanderKills: Int,
    val infinite: Boolean,
    val bodyGuard: Int,
    val penalty: Int
) {
    fun points(pointSystem: PointSystem, deckTier: Tier): Int {
        val place = (pointSystem.placeScore[place - 1] * deckTier.multiplier).toInt()
        val infinitePenalty = if (infinite) pointSystem.infinite else 0
        return place + kills * pointSystem.kill + commanderKills * pointSystem.commanderKill +
                bodyGuard * pointSystem.bodyGuard - infinitePenalty - penalty
    }
}

data class Season(val id: Int, val players: List<Pair<String, Int>>)

data class PointSystem(
    val placeScore: List<Int>, val kill: Int, val commanderKill: Int, val infinite: Int, val bodyGuard: Int
)

val DEFAULT_POINT_SYSTEM = PointSystem(listOf(4, 2, 1, 0), 2, 1, 2, 1)

data class NewSeasonRequest(
    val player1: String, val player2: String, val player3: String, val player4: String,
    val pointSystem: PointSystem = DEFAULT_POINT_SYSTEM
)

data class Stats(
    val gamesPlayed: Int,
    val gamesWon: Int,
    val gamesWonWhenFirst: Int,
    val gamesWonWhenSecond: Int,
    val gamesWonWhenThird: Int,
    val gamesWonWhenFourth: Int,
    val gamesWonWithInfinite: Int,
    val avgPlace: Double,
    val avgKills: Double,
    val avgCommanderKills: Double
)

data class DeckStats(
    val deckName: String,
    val stats: Stats
)

data class PlayerStats(
    val playerName: String,
    val avgDeckStats: List<DeckStats>,
    val avgStats: Stats,
    val deckStatsPerSeason: Map<Int, List<DeckStats>>,
    val statsPerSeason: Map<Int, Stats>
)

data class SeasonStats(
    val avgStats: Stats,
    val deckStats: List<DeckStats>
)

fun List<Stats>.calculateAverage(): Stats = Stats(
    sumOf { it.gamesPlayed },
    sumOf { it.gamesWon },
    sumOf { it.gamesWonWhenFirst },
    sumOf { it.gamesWonWhenSecond },
    sumOf { it.gamesWonWhenThird },
    sumOf { it.gamesWonWhenFourth },
    sumOf { it.gamesWonWithInfinite },
    sumOf { it.avgPlace } / size,
    sumOf { it.avgKills } / size,
    sumOf { it.avgCommanderKills } / size
)
