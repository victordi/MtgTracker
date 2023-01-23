package com.mtg.tracker.security

import arrow.core.Either
import arrow.core.continuations.either
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mtg.tracker.data.SignedJWT
import com.mtg.tracker.data.isNotExpired
import com.mtg.tracker.failure.Failure
import com.mtg.tracker.failure.InvalidLoginCredentials
import com.mtg.tracker.failure.JwtSerializationException
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.keys.AesKey
import org.jose4j.lang.ByteUtil
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

object JwtService {
    private val jwtKey = AesKey(ByteUtil.randomBytes(32))
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun String.createClaims() = JwtClaims().apply {
        issuer = "MtgTracker"
        setGeneratedJwtId()
        issuedAt = NumericDate.now()
        setExpirationTimeMinutesInTheFuture(360f)
        subject = this@createClaims
    }.toJson()

    fun generateJwt(username: String): Either<Failure, String> = runSafe {
        JsonWebEncryption()
            .apply {
                algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.DIRECT
                payload = username.createClaims()
                encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256
                keyIdHeaderValue = "MtgTracker"
                key = jwtKey
            }
            .compactSerialization
    }

    fun decrypt(jwt: String): Either<Failure, Authentication> = either.eager {
        val jwe = JsonWebEncryption().apply {
            setAlgorithmConstraints(
                AlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    KeyManagementAlgorithmIdentifiers.DIRECT
                )
            )
            setContentEncryptionAlgorithmConstraints(
                AlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256
                )
            )
            runSafe { compactSerialization = jwt }.bind()
            key = jwtKey
        }
        val obj = runSafe { jwe.payload }.bind()
        val signedJWT = runSafe { jacksonObjectMapper().readValue(obj, SignedJWT::class.java) }.bind()
        ensure(signedJWT.isNotExpired) {
            logger.error("Could not decrypt jwt because it is expired")
            InvalidLoginCredentials
        }
        UsernamePasswordAuthenticationToken(signedJWT.sub, "", emptyList())
    }

    private inline fun <reified T> runSafe(f: () -> T): Either<Failure, T> = Either
        .catch { f() }
        .tapLeft { logger.error(it.message, it) }
        .mapLeft { JwtSerializationException }
}