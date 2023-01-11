package com.mtg.tracker.security.filter

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager : ReactiveAuthenticationManager {
  override fun authenticate(
    authentication: Authentication?
  ): Mono<Authentication> = Mono.justOrEmpty(authentication)
}
