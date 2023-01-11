package com.mtg.tracker.security.config

import com.mtg.tracker.security.filter.JwtAuthenticationConverter
import com.mtg.tracker.security.filter.JwtAuthenticationManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@EnableWebFluxSecurity
@Configuration
class WebSecurityConfiguration(
  private val authManager: JwtAuthenticationManager,
  private val authConverter: JwtAuthenticationConverter
) {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    val authFilter = AuthenticationWebFilter(authManager).apply {
      setServerAuthenticationConverter(authConverter)
    }

    return http
      .csrf().disable()
      .authorizeExchange()
      .pathMatchers("/login").permitAll()
      .anyExchange().authenticated()
      .and().addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION).build()
  }
}
