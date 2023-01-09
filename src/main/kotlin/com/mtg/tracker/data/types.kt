package com.mtg.tracker.data

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
    val player1: String, val player2: String, val player3: String, val player4: String, val pointSystem: PointSystem
)
