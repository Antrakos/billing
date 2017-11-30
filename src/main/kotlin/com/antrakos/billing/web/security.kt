package com.antrakos.billing.web

import com.antrakos.billing.models.Role
import com.antrakos.billing.repository.UserRepository
import com.antrakos.billing.service.impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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
                .antMatchers(HttpMethod.POST, "/customer/").permitAll()
                .antMatchers(HttpMethod.DELETE, "/customer/**/service/**").hasRole(Role.WORKER.name)
                .antMatchers(HttpMethod.POST, "/service/").hasRole(Role.WORKER.name)
                .antMatchers(HttpMethod.DELETE, "/service/**").hasRole(Role.WORKER.name)
                .antMatchers(HttpMethod.POST, "/usage/").hasRole(Role.WORKER.name)
                .anyRequest().authenticated()
                .and().httpBasic()
                .authenticationEntryPoint { _, response, authException ->
                    response.addHeader("WWW-Authenticate", "Basic realm=Billing")
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.writer.println("HTTP Status 400 - " + authException.message)
                }
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**")
    }

    @Bean
    open fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    open fun authenticationProvider(userRepository: UserRepository) = DaoAuthenticationProvider().apply {
        setUserDetailsService(UserServiceImpl(userRepository, passwordEncoder()))
        setPasswordEncoder(passwordEncoder())
    }

    @Autowired
    private lateinit var authenticationProvider: AuthenticationProvider

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authenticationProvider)
    }
}

class SecureUser(val id: Int, private val username: String, private val password: String, private val role: Role, private val enabled: Boolean, private val customerId: Int?) : UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority("ROLE_" + role))

    override fun isEnabled() = enabled

    override fun getUsername() = username

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = password

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    fun checkAccess(customerId: Int): SecureUser {
        if (role == Role.CUSTOMER && this.customerId != customerId)
            throw IllegalAccessException("You have access only to yours resources")
        return this
    }
}