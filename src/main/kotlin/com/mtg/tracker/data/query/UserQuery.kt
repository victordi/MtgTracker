package com.mtg.tracker.data.query

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.leftIfNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mtg.tracker.data.SignedJWT
import com.mtg.tracker.data.User
import com.mtg.tracker.data.Users
import com.mtg.tracker.data.isNotExpired
import com.mtg.tracker.data.safeTransaction
import com.mtg.tracker.failure.DatabaseFailure
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.InvalidLoginCredentials
import com.mtg.tracker.security.JwtService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithm
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.keys.AesKey
import org.jose4j.lang.ByteUtil
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object UserQuery {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun login(username: String, pwdCheck: (String) -> Boolean): Either<Failure, String> = safeTransaction {
        Users.select { Users.username eq username }
            .map { it[Users.username] to it[Users.password]}
            .firstOrNull()
    }
        .map { if (it != null && pwdCheck(it.second)) it.first else null }
        .tapLeft { logger.error(it.message) }
        .mapLeft { DatabaseFailure }
        .leftIfNull { InvalidLoginCredentials.also { logger.error("Invalid logging credentials for $username") } }
        .flatMap { JwtService.generateJwt(it) }
}
