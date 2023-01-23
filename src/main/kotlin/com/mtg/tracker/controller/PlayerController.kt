package com.mtg.tracker.controller

import com.mtg.tracker.data.query.PlayerQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/players")
class PlayerController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    suspend fun getAll() = run {
        logger.info("Retrieve all players from database")
        PlayerQuery.findAll().toHttpResponse()
    }

    @GetMapping("/{name}")
    suspend fun getByName(@PathVariable("name") name: String) = run {
        logger.info("Get player by name: $name")
        PlayerQuery.findByName(name).toHttpResponse()
    }

    @PostMapping
    suspend fun addPlayer(@RequestBody body: NameReq) = run {
        logger.info("Insert new player: ${body.name}")
        PlayerQuery.insert(body.name).toHttpResponse()
    }

    @DeleteMapping("/{name}")
    suspend fun removePlayer(@PathVariable("name") name: String) = run {
        logger.info("Delete player: $name")
        PlayerQuery.delete(name).toHttpResponse()
    }

    @GetMapping("/{name}/stats")
    suspend fun getStats(@PathVariable("name") name: String) = run {
        logger.info("Get stats of player: $name")
        PlayerQuery.stats(name).toHttpResponse()
    }
}
