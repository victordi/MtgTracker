package com.mtg.tracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux
@EnableWebFlux
@SpringBootApplication
class MtgTrackerApplication

fun main(args: Array<String>) {
	runApplication<MtgTrackerApplication>(*args)
}
