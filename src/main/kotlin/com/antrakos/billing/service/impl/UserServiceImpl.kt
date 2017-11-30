package com.antrakos.billing.service.impl

import com.antrakos.billing.models.User
import com.antrakos.billing.repository.UserRepository
import com.antrakos.billing.service.UserService
import com.antrakos.billing.web.SecureUser
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * @author Taras Zubrei
 */
@Service
open class UserServiceImpl(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) : UserService {
    override fun create(user: User) = userRepository.save(user.copy(password = passwordEncoder.encode(user.password)))

    override fun loadUserByUsername(username: String) = userRepository.findByUsername(username)?.let {
        SecureUser(it.id!!, it.username, it.password, it.role, it.enabled, it.customerId)
    } ?: throw UsernameNotFoundException("No user found for username=$username")
}