package com.mtg.tracker.security.filter

import com.mtg.tracker.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter : ServerAuthenticationConverter {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun convert(exchange: ServerWebExchange): Mono<Authentication?> =
    Mono
      .justOrEmpty(exchange.request.headers.getFirst("Authorization"))
      .filter { it.startsWith("Bearer ") }
      .mapNotNull { header -> JwtService.decrypt(header.removePrefix("Bearer ")).fold({ null }, { it } )}
      .doOnError { log.error(it.message) }
      .onErrorResume { Mono.empty() }
}
