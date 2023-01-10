package com.mtg.tracker.controller

import arrow.core.Either
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.DeckNotFound
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.NameNotUniqueFailure
import com.mtg.tracker.failure.PlayerNotFound
import com.mtg.tracker.failure.PlayerNotInSeason
import com.mtg.tracker.failure.SeasonNotFound
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import java.time.Clock
import java.time.Instant

sealed interface Response

data class Success<out T>(val data: T) : Response
data class Error(
    val status: Int,
    val message: String,
    val timestamp: Long = Instant.now(Clock.systemUTC()).epochSecond
) : Response

inline fun <reified A> Either<Failure, A>.toHttpResponse(): ResponseEntity<Response> = fold(
    { failure ->
        val error = failure.toError()
        ResponseEntity(error, HttpStatus.valueOf(error.status))
    },
    { ResponseEntity(Success(it), OK) }
)

fun Failure.toError(): Error = run {
    val status= when (this) {
        is DatabaseFailure -> INTERNAL_SERVER_ERROR
        is NameNotUniqueFailure -> BAD_REQUEST
        is PlayerNotFound -> NOT_FOUND
        is DeckNotFound -> NOT_FOUND
        is SeasonNotFound -> NOT_FOUND
        is PlayerNotInSeason -> NOT_FOUND
    }.value()
    Error(status, message)
}