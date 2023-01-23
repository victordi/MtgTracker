package com.mtg.tracker.controller

import com.mtg.tracker.data.Deck
import com.mtg.tracker.data.query.DeckQuery
import com.mtg.tracker.data.query.PlayerQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/players/{name}/decks")
class DeckController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    suspend fun getAll(@PathVariable("name") name: String) = run {
        logger.info("Retrieve all decks of Player($name) from database")
        DeckQuery.findAll(name).toHttpResponse()
    }

    @PostMapping
    suspend fun addDeck(@PathVariable("name") name: String, @RequestBody deck: Deck) = run {
        logger.info("Add deck: $deck for Player($name)")
        DeckQuery.insert(deck, name).toHttpResponse()
    }

    @PatchMapping
    suspend fun updateTier(@PathVariable("name") name: String, @RequestBody deck: Deck) = run {
        logger.info("Update deck: $deck for Player($name)")
        DeckQuery.updateTier(name, deck).toHttpResponse()
    }

    @DeleteMapping("/{deckName}")
    suspend fun removeDeck(@PathVariable("name") name: String, @PathVariable deckName: String) = run {
        logger.info("Deleting deck: $deckName of Player($name)")
        DeckQuery.delete(name, deckName).toHttpResponse()
    }
}
