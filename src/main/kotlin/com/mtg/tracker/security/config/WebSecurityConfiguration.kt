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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@EnableWebFluxSecurity
@Configuration
class WebSecurityConfiguration(
  private val authManager: JwtAuthenticationManager,
  private val authConverter: JwtAuthenticationConverter
) {

  private val corsConfig = CorsConfiguration().apply {
    allowedOrigins = listOf("http://localhost:3000")
    maxAge = 8000L
    allowedMethods = listOf("*")
    allowedHeaders = listOf("*")
  }

  private val source = UrlBasedCorsConfigurationSource().apply {
    registerCorsConfiguration("/**", corsConfig)
  }
  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    val authFilter = AuthenticationWebFilter(authManager).apply {
      setServerAuthenticationConverter(authConverter)
    }

    return http
      .csrf().disable()
      .cors {
        it.configurationSource(source)
      }
      .authorizeExchange()
      .pathMatchers("/login").permitAll()
      .anyExchange().authenticated()
      .and().addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
      .build()
  }
}
