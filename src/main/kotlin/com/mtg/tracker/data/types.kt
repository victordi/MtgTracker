package com.mtg.tracker.data

enum class Tier(val multiplier: Double) {
    I(1.0), II(1.5), III(2.0)
}

data class Player(val name: String, val decks: List<Deck>)

data class Deck(val name: String, val tier: Tier)

data class GameResult(
    val id: Int,
    val seasonId: Int,
    val deckName: String,
    val place: Int,
    val startOrder: Int,
    val kills: Int,
    val commanderKills: Int,
    val infinite: Boolean,
    val bodyGuard: Int,
    val penalty: Int
)

data class Season(val id: Int, val players: Map<String, Int>)