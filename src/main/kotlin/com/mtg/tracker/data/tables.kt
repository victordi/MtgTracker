package com.mtg.tracker.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

const val MAX_STRING_LENGTH = 255

object Players : Table("PLAYER") {
    val name = varchar("NAME", MAX_STRING_LENGTH)

    override val primaryKey = PrimaryKey(name)
}

object Decks : Table("DECK") {
    val name = varchar("NAME", MAX_STRING_LENGTH)
    val tier = enumerationByName<Tier>("TIER", MAX_STRING_LENGTH)
    val playerName = varchar("PLAYER_NAME", MAX_STRING_LENGTH)
        .references(Players.name)

    override val primaryKey = PrimaryKey(Players.name)
}

object Seasons: IntIdTable("SEASON") {
    val player1 = varchar("PLAYER1", MAX_STRING_LENGTH).references(Players.name)
    val points1 = integer("POINTS1")
    val player2 = varchar("PLAYER2", MAX_STRING_LENGTH).references(Players.name)
    val points2 = integer("POINTS2")
    val player3 = varchar("PLAYER3", MAX_STRING_LENGTH).references(Players.name)
    val points3 = integer("POINTS3")
    val player4 = varchar("PLAYER4", MAX_STRING_LENGTH).references(Players.name)
    val points4 = integer("POINTS4")
}

object GameResults : IntIdTable("GAME_RESULT") {
    val seasonId = integer("SEASON_ID").references(Seasons.id)
    val playerName = varchar("PLAYER_NAME", MAX_STRING_LENGTH).references(Players.name)
    val deckName = varchar("DECK_NAME", MAX_STRING_LENGTH).references(Decks.name)
    val place = integer("PLACE")
    val order = integer("START_ORDER")
    val kills = integer("KILLS")
    val commanderKills = integer("COMMANDER_KILLS")
    val infinite = bool("INFINITE")
    val bodyGuard = integer("BODY_GUARD")
    val penalty = integer("PENALTY")
}