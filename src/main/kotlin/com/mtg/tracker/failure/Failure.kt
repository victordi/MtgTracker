package com.mtg.tracker.failure

sealed class Failure(open val message: String)

object DatabaseFailure : Failure("Failed to execute database query")