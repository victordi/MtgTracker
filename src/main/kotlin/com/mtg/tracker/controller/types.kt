package com.mtg.tracker.controller

import com.mtg.tracker.data.PointSystem
import com.mtg.tracker.data.Tier

data class NameReq(val name: String)

data class NewGameResultReq(
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
        val placeScore = when(place) {
            1 -> pointSystem.firstPlace
            2 -> pointSystem.secondPlace
            3 -> pointSystem.thirdPlace
            else -> pointSystem.fourthPlace
        }
        val place = (placeScore * deckTier.multiplier).toInt()
        val infinitePenalty = if (infinite) pointSystem.infinite else 0
        return place + kills * pointSystem.kill + commanderKills * pointSystem.commanderKill +
                bodyGuard * pointSystem.bodyGuard - infinitePenalty - penalty
    }
}
