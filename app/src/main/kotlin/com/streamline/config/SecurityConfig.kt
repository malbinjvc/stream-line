package com.streamline.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // API-only, stateless
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers ->
                headers.xssProtection { it.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK) }
                headers.contentTypeOptions { }
                headers.frameOptions { it.deny() }
                headers.httpStrictTransportSecurity { it.includeSubDomains(true).maxAgeInSeconds(31536000) }
                headers.permissionsPolicy { it.policy("geolocation=(), camera=(), microphone=()") }
                headers.contentSecurityPolicy { it.policyDirectives("default-src 'none'; frame-ancestors 'none'") }
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/**").permitAll()
                auth.requestMatchers("/actuator/health").permitAll()
                auth.anyRequest().denyAll()
            }

        return http.build()
    }
}
