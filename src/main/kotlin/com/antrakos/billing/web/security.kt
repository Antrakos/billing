package com.antrakos.billing.web

import com.antrakos.billing.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletResponse

/**
 * @author Taras Zubrei
 */
@Configuration
@EnableWebSecurity
open class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and().httpBasic()
                .authenticationEntryPoint { _, response, authException ->
                    response.addHeader("WWW-Authenticate", "Basic realm=Billing")
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.writer.println("HTTP Status 400 - " + authException.message)
                }
    }

    @Bean
    open fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    open fun authenticationProvider(userDetailsService: UserDetailsService, passwordEncoder: PasswordEncoder) = DaoAuthenticationProvider().apply {
        setUserDetailsService(userDetailsService)
        setPasswordEncoder(passwordEncoder)
    }

    @Autowired
    private lateinit var authenticationProvider: AuthenticationProvider

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authenticationProvider)
    }
}

@Service
open class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String) = userRepository.findByUsername(username)?.let {
        User
                .withUsername(it.username)
                .password(it.password)
                .roles(it.role.name)
                .disabled(!it.enabled)
                .build()
    } ?: throw UsernameNotFoundException("No user found for username=$username")
}