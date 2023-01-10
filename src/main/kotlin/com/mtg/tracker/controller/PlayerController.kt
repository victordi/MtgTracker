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
    suspend fun getByName(@PathVariable("name") body: PlayerReq) = run {
        logger.info("Get player by name: ${body.name}")
        PlayerQuery.findByName(body.name).toHttpResponse()
    }

    @PostMapping
    suspend fun addPlayer(@RequestBody body: PlayerReq) = run {
        logger.info("Insert new player: ${body.name}")
        PlayerQuery.insert(body.name).toHttpResponse()
    }

    @DeleteMapping
    suspend fun removePlayer(@RequestBody body: PlayerReq) = run {
        logger.info("Delete player: ${body.name}")
        PlayerQuery.delete(body.name).toHttpResponse()
    }

    @GetMapping("/{name}/stats")
    suspend fun getStats(@PathVariable("name") body: PlayerReq) = run {
        logger.info("Get stats of player: ${body.name}")
        PlayerQuery.stats(body.name).toHttpResponse()
    }
}
