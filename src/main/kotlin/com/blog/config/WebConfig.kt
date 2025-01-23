package com.blog.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**"
        ).addResourceLocations(
            "classpath:/static/css/",
            "classpath:/static/js/",
            "classpath:/static/images/",
            "classpath:/META-INF/resources/webjars/"
        )
    }
} 