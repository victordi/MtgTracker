package com.mtg.tracker.controller

import com.mtg.tracker.data.User
import com.mtg.tracker.data.query.UserQuery
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class LoginController {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val encoder = BCryptPasswordEncoder()

    @PostMapping("/login")
    suspend fun login(@RequestBody user: User) = run {
        logger.info("Received login request from ${user.username}")
        UserQuery
            .login(user.username) { encoder.matches(user.password, it) }
            .toHttpResponse()
    }
}
