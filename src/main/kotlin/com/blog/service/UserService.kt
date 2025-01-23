package com.blog.service

import com.blog.model.User
import com.blog.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    @PostConstruct
    fun init() {
        if (userRepository.findByUsername("admin") == null) {
            val admin = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                role = "ADMIN"
            )
            userRepository.save(admin)
        }
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Usuário não encontrado")

        return org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.password)
            .roles(user.role)
            .build()
    }
} 