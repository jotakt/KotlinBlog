package com.blog.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ITemplateResolver

@Configuration
class ThymeleafConfig {

    @Bean
    fun templateResolver(): ITemplateResolver {
        val resolver = SpringResourceTemplateResolver()
        resolver.prefix = "classpath:/templates/"
        resolver.suffix = ".html"
        resolver.templateMode = TemplateMode.HTML
        resolver.characterEncoding = "UTF-8"
        resolver.isCacheable = false
        resolver.checkExistence = true
        return resolver
    }

    @Bean
    fun templateEngine(): SpringTemplateEngine {
        val engine = SpringTemplateEngine()
        engine.setTemplateResolver(templateResolver())
        engine.addDialect(Java8TimeDialect())
        engine.addDialect(SpringSecurityDialect())
        return engine
    }
} 