package com.blog.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    var title: String = "",
    
    @Column(columnDefinition = "TEXT")
    var content: String = "",
    
    var author: String = "",
    
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 