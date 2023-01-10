package com.mtg.tracker.controller

import com.mtg.tracker.data.NewSeasonRequest
import com.mtg.tracker.data.query.SeasonQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seasons")
class SeasonController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    suspend fun getAll() = run {
        logger.info("Retrieve all seasons")
        SeasonQuery.find().toHttpResponse()
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable("id") id: Int) = run {
        logger.info("Retrieve Season($id)")
        SeasonQuery.find(id).toHttpResponse()
    }

    @PostMapping
    suspend fun startSeason(@RequestBody seasonRequest: NewSeasonRequest) = run {
        logger.info("Starting a new season: $seasonRequest")
        SeasonQuery.startSeason(seasonRequest).toHttpResponse()
    }
}