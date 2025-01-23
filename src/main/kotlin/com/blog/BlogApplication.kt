package com.blog

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.util.ResourceUtils
import jakarta.annotation.PostConstruct

@SpringBootApplication
class BlogApplication {
    private val logger = LoggerFactory.getLogger(BlogApplication::class.java)

    @PostConstruct
    fun init() {
        logger.info("Verificando recursos estáticos...")
        try {
            val resource = ResourceUtils.getFile("classpath:static/css/output.css")
            logger.info("output.css existe: ${resource.exists()}")
        } catch (e: Exception) {
            logger.error("Erro ao verificar output.css: ${e.message}")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<BlogApplication>(*args)
} 