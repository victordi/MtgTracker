package com.mtg.tracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MtgTrackerApplication

fun main(args: Array<String>) {
	runApplication<MtgTrackerApplication>(*args)
}
