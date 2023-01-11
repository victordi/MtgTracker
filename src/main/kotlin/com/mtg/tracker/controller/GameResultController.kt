package com.mtg.tracker.controller

import com.mtg.tracker.data.GameResult
import com.mtg.tracker.data.NewSeasonRequest
import com.mtg.tracker.data.query.GameResultQuery
import com.mtg.tracker.data.query.SeasonQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/results")
class GameResultController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    suspend fun submitGameResult(@RequestBody gameResult: GameResult) = run {
        logger.info("Storing the following game result: $gameResult")
        GameResultQuery.reportGame(gameResult).toHttpResponse()
    }
}