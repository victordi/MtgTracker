package com.mtg.tracker.failure

sealed class Failure(open val message: String)

object DatabaseFailure : Failure("Failed to execute database query")
object NameNotUniqueFailure : Failure("This name is already registered")
object PlayerNotFound : Failure("No player with that name is registered")