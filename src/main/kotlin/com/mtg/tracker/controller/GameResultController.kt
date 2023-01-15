package com.mtg.tracker.controller

import com.mtg.tracker.data.query.GameResultQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seasons/{id}/results")
class GameResultController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    suspend fun findAll(@PathVariable("id") id: Int) = run {
        logger.info("Retrieving all game results from Seasons $id")
        GameResultQuery.findAll(id).toHttpResponse()
    }

    @PostMapping
    suspend fun submitGameResult(@PathVariable("id") id: Int, @RequestBody gameResult: NewGameResultReq) = run {
        logger.info("Storing the following game result: $gameResult")
        GameResultQuery.reportGame(id, gameResult).toHttpResponse()
    }

    @DeleteMapping("/{gameId}")
    suspend fun delete(@PathVariable("id") id: Int, @PathVariable("gameId") gameId: Int) = run {
        logger.info("Deleting Game result with id $gameId from Seasons $id")
        GameResultQuery.deleteGame(id, gameId).toHttpResponse()
    }
}