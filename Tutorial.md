# Tutorial: Blog com CMS em Kotlin e Spring Boot

Este tutorial irá guiá-lo na criação de um blog com sistema de CMS usando Kotlin, Spring Boot e Tailwind CSS.

## Pré-requisitos

- JDK 17 ou superior
- IntelliJ IDEA (recomendado) ou outra IDE
- Gradle
- Git (opcional)

## 1. Configuração do Projeto

### 1.1 Criar novo projeto Spring Boot

1. Acesse [Spring Initializr](https://start.spring.io/)
2. Configure o projeto:
   - Project: Gradle - Kotlin
   - Language: Kotlin
   - Spring Boot: 3.2.3
   - Group: com.blog
   - Artifact: blog
   - Name: blog
   - Description: Blog com CMS
   - Package name: com.blog
   - Packaging: Jar
   - Java: 17

3. Adicione as dependências:
   - Spring Web
   - Spring Data JPA
   - H2 Database
   - Spring Security
   - Thymeleaf
   - Spring Boot DevTools (opcional)

4. Clique em "Generate" e extraia o arquivo ZIP

### 1.2 Configurar build.gradle.kts
kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
id("org.springframework.boot") version "3.2.3"
id("io.spring.dependency-management") version "1.1.4"
kotlin("jvm") version "1.9.22"
kotlin("plugin.spring") version "1.9.22"
kotlin("plugin.jpa") version "1.9.22"
}
group = "com.blog"
version = "0.0.1-SNAPSHOT"
java {
sourceCompatibility = JavaVersion.VERSION_17
}
repositories {
mavenCentral()
}
dependencies {
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
implementation("org.jetbrains.kotlin:kotlin-reflect")
implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
implementation("com.h2database:h2")
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.security:spring-security-test")
}
tasks.withType<KotlinCompile> {
kotlinOptions {
freeCompilerArgs += "-Xjsr305=strict"
jvmTarget = "17"
}
}
tasks.withType<Test> {
useJUnitPlatform()
}

### 1.3 Configurar application.properties
```properties
spring.datasource.url=jdbc:h2:file:./blogdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## 2. Estrutura do Projeto

### 2.1 Criar Modelos

Post.kt:
```kotlin
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
```

User.kt:
```kotlin
package com.blog.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true)
    val username: String,
    
    val password: String,
    
    val role: String = "ADMIN"
)
```

### 2.2 Criar Repositórios

PostRepository.kt:
```kotlin
package com.blog.repository

import com.blog.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long>
```

UserRepository.kt:
```kotlin
package com.blog.repository

import com.blog.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}
```

### 2.3 Configurar Segurança

SecurityConfig.kt:
```kotlin
package com.blog.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/", permitAll)
                authorize("/post/**", permitAll)
                authorize("/css/**", permitAll)
                authorize("/js/**", permitAll)
                authorize("/admin/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            formLogin {
                loginPage = "/login"
                defaultSuccessUrl("/admin", true)
                permitAll()
            }
            logout {
                logoutSuccessUrl = "/"
                permitAll()
            }
            csrf {
                disable()
            }
        }

        return http.build()
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
```

### 2.4 Criar Serviço de Usuário

UserService.kt:
```kotlin
package com.blog.service

import com.blog.model.User
import com.blog.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

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
```

### 2.5 Criar Controllers

BlogController.kt:
```kotlin
package com.blog.controller

import com.blog.repository.PostRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class BlogController(private val postRepository: PostRepository) {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("posts", postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")))
        return "blog/home"
    }

    @GetMapping("/post/{id}")
    fun post(@PathVariable id: Long, model: Model): String {
        val post = postRepository.findById(id).orElseThrow()
        model.addAttribute("post", post)
        return "blog/post"
    }
}
```

AdminController.kt:
```kotlin
package com.blog.controller

import com.blog.model.Post
import com.blog.repository.PostRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Controller
@RequestMapping("/admin")
class AdminController(private val postRepository: PostRepository) {

    @GetMapping
    fun dashboard(model: Model): String {
        model.addAttribute("posts", postRepository.findAll())
        return "admin/dashboard"
    }

    @GetMapping("/post/new")
    fun newPost(model: Model): String {
        model.addAttribute("post", Post())
        return "admin/edit"
    }

    @PostMapping("/post/save")
    fun savePost(@ModelAttribute post: Post): String {
        val now = LocalDateTime.now()
        
        if (post.id == 0L) {
            post.createdAt = now
        }
        post.updatedAt = now
        
        postRepository.save(post)
        return "redirect:/admin"
    }

    @GetMapping("/post/edit/{id}")
    fun editPost(@PathVariable id: Long, model: Model): String {
        val post = postRepository.findById(id).orElseThrow()
        model.addAttribute("post", post)
        return "admin/edit"
    }

    @PostMapping("/post/delete/{id}")
    fun deletePost(@PathVariable id: Long): String {
        postRepository.deleteById(id)
        return "redirect:/admin"
    }
}
```

AuthController.kt:
```kotlin
package com.blog.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthController {

    @GetMapping("/login")
    fun login(): String = "auth/login"
}
```

## 3. Templates HTML

Criar os seguintes arquivos na pasta `src/main/resources/templates/`:

1. layout.html (template base)
2. auth/login.html (página de login)
3. blog/home.html (página inicial do blog)
4. blog/post.html (página individual do post)
5. admin/dashboard.html (painel administrativo)
6. admin/edit.html (formulário de edição/criação de post)

## 4. Executando o Projeto

1. Abra o projeto na sua IDE
2. Execute a classe BlogApplication.kt
3. Acesse http://localhost:8080

Credenciais padrão:
- Usuário: admin
- Senha: admin123

## 5. Estilização (Opcional)

Para adicionar o tema escuro e estilos modernos, siga os passos do arquivo de estilização fornecido separadamente.

## 6. Estilização com Tailwind CSS

### 6.1 Configuração do Tailwind CSS

1. Primeiro, crie o arquivo de configuração do Tailwind:

```javascript:src/main/resources/static/js/tailwind.config.js
/** @type {import('tailwindcss').Config} */
module.exports = {
    darkMode: 'class',
    content: [
        "./src/**/*.html",
        "./src/**/*.js"
    ],
    theme: {
        extend: {
            colors: {
                dark: {
                    50: '#f8fafc',
                    100: '#f1f5f9',
                    200: '#e2e8f0',
                    300: '#cbd5e1',
                    400: '#94a3b8',
                    500: '#64748b',
                    600: '#475569',
                    700: '#334155',
                    800: '#1e293b',
                    900: '#0f172a',
                }
            }
        }
    },
    plugins: [
        require('@tailwindcss/typography')
    ]
}
```

2. Atualize o layout.html para incluir o Tailwind CSS e a fonte Inter:

```html:src/main/resources/templates/layout.html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security"
      class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Blog</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
        }
    </style>
</head>
<body class="bg-dark-900 text-gray-100 min-h-screen">
    <nav class="bg-dark-800 border-b border-dark-700">
        <div class="max-w-6xl mx-auto px-4">
            <div class="flex justify-between h-16">
                <div class="flex items-center">
                    <a href="/" class="flex items-center">
                        <span class="text-2xl font-bold bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent">Blog</span>
                    </a>
                </div>
                <div class="flex items-center space-x-4">
                    <a sec:authorize="!isAuthenticated()" 
                       href="/login" 
                       class="px-4 py-2 rounded-lg text-gray-300 hover:text-white hover:bg-dark-700 transition-colors">
                        Login
                    </a>
                    <a sec:authorize="hasRole('ADMIN')" 
                       href="/admin" 
                       class="px-4 py-2 rounded-lg text-gray-300 hover:text-white hover:bg-dark-700 transition-colors">
                        Admin
                    </a>
                    <form sec:authorize="isAuthenticated()" 
                          th:action="@{/logout}" 
                          method="post" 
                          class="inline">
                        <button type="submit" 
                                class="px-4 py-2 rounded-lg text-red-400 hover:text-red-300 hover:bg-dark-700 transition-colors">
                            Sair
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </nav>
    
    <main class="container mx-auto px-4 py-8">
        <div th:replace="${content}">
            <!-- Conteúdo será inserido aqui -->
        </div>
    </main>
</body>
</html>
```

### 6.2 Estilização das Páginas

1. Página inicial (blog/home.html):
```html:src/main/resources/templates/blog/home.html
<div th:fragment="content">
    <div class="max-w-4xl mx-auto">
        <h1 class="text-4xl font-bold mb-12 bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent">
            Posts Recentes
        </h1>
        
        <div class="space-y-8">
            <div th:each="post : ${posts}" 
                 class="bg-dark-800 rounded-xl shadow-lg overflow-hidden border border-dark-700 hover:border-dark-600 transition-colors">
                <div class="p-8">
                    <h2 class="text-2xl font-bold mb-3">
                        <a th:href="@{/post/{id}(id=${post.id})}" 
                           th:text="${post.title}"
                           class="text-gray-100 hover:text-blue-400 transition-colors"></a>
                    </h2>
                    <div class="text-sm text-gray-400 mb-4">
                        <span th:text="${post.author}"></span> • 
                        <span th:text="${#temporals.format(post.createdAt, 'dd/MM/yyyy')}"></span>
                    </div>
                    <p class="text-gray-300 mb-4" 
                       th:text="${#strings.abbreviate(post.content, 300)}"></p>
                    <a th:href="@{/post/{id}(id=${post.id})}" 
                       class="inline-flex items-center text-blue-400 hover:text-blue-300 transition-colors">
                        Ler mais 
                        <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/>
                        </svg>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
```

2. Página do post (blog/post.html):
```html:src/main/resources/templates/blog/post.html
<div th:fragment="content">
    <div class="max-w-3xl mx-auto">
        <article class="bg-dark-800 rounded-xl shadow-lg overflow-hidden border border-dark-700">
            <div class="p-8">
                <h1 class="text-4xl font-bold mb-4 bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent" 
                    th:text="${post.title}"></h1>
                <div class="text-gray-400 mb-8">
                    <span th:text="${post.author}"></span> • 
                    <span th:text="${#temporals.format(post.createdAt, 'dd/MM/yyyy')}"></span>
                </div>
                <div class="prose prose-invert max-w-none text-gray-300" 
                     th:utext="${#strings.replace(#strings.escapeXml(post.content), '&#10;', '<br/>')}">
                </div>
            </div>
        </article>
        
        <div class="mt-8">
            <a href="/" class="inline-flex items-center text-blue-400 hover:text-blue-300 transition-colors">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
                </svg>
                Voltar para a página inicial
            </a>
        </div>
    </div>
</div>
```

3. Página de login (auth/login.html):
```html:src/main/resources/templates/auth/login.html
<div th:fragment="content">
    <div class="min-h-[80vh] flex items-center justify-center">
        <div class="max-w-md w-full space-y-8 bg-dark-800 p-8 rounded-xl shadow-lg border border-dark-700">
            <div>
                <h2 class="text-3xl font-bold text-center bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent">
                    Login Administrativo
                </h2>
            </div>
            <form class="mt-8 space-y-6" action="/login" method="POST">
                <div class="space-y-4">
                    <div>
                        <label for="username" class="sr-only">Usuário</label>
                        <input id="username" name="username" type="text" required 
                               class="w-full bg-dark-700 border border-dark-600 rounded-lg px-4 py-3 text-gray-100 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors" 
                               placeholder="Usuário">
                    </div>
                    <div>
                        <label for="password" class="sr-only">Senha</label>
                        <input id="password" name="password" type="password" required 
                               class="w-full bg-dark-700 border border-dark-600 rounded-lg px-4 py-3 text-gray-100 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors" 
                               placeholder="Senha">
                    </div>
                </div>

                <div>
                    <button type="submit" 
                            class="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                        Entrar
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
```

4. Painel administrativo (admin/dashboard.html):
```html:src/main/resources/templates/admin/dashboard.html
<div th:fragment="content">
    <div class="max-w-7xl mx-auto">
        <div class="mb-8 flex justify-between items-center">
            <h1 class="text-3xl font-bold bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent">
                Painel Administrativo
            </h1>
            <a href="/admin/post/new" 
               class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                Novo Post
            </a>
        </div>

        <div class="bg-dark-800 rounded-xl shadow-lg overflow-hidden border border-dark-700">
            <table class="min-w-full divide-y divide-dark-700">
                <thead>
                    <tr class="bg-dark-900/50">
                        <th class="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                            Título
                        </th>
                        <th class="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                            Autor
                        </th>
                        <th class="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                            Data
                        </th>
                        <th class="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                            Ações
                        </th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-dark-700">
                    <tr th:each="post : ${posts}" class="hover:bg-dark-700/50 transition-colors">
                        <td class="px-6 py-4 whitespace-nowrap text-gray-300" th:text="${post.title}"></td>
                        <td class="px-6 py-4 whitespace-nowrap text-gray-300" th:text="${post.author}"></td>
                        <td class="px-6 py-4 whitespace-nowrap text-gray-300" 
                            th:text="${#temporals.format(post.createdAt, 'dd/MM/yyyy')}"></td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            <a th:href="@{/admin/post/edit/{id}(id=${post.id})}" 
                               class="text-blue-400 hover:text-blue-300 mr-4 transition-colors">Editar</a>
                            <form th:action="@{/admin/post/delete/{id}(id=${post.id})}" 
                                  method="POST" 
                                  class="inline">
                                <button type="submit" 
                                        class="text-red-400 hover:text-red-300 transition-colors"
                                        onclick="return confirm('Tem certeza que deseja excluir este post?')">
                                    Excluir
                                </button>
                            </form>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
```

5. Formulário de edição (admin/edit.html):
```html:src/main/resources/templates/admin/edit.html
<div th:fragment="content">
    <div class="max-w-3xl mx-auto">
        <h1 class="text-3xl font-bold mb-8 bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent" 
            th:text="${post.id == 0 ? 'Novo Post' : 'Editar Post'}"></h1>
        
        <div class="bg-dark-800 rounded-xl shadow-lg p-8 border border-dark-700">
            <form th:action="@{/admin/post/save}" method="POST" th:object="${post}">
                <input type="hidden" th:field="*{id}" />
                
                <div class="mb-6">
                    <label class="block text-gray-300 text-sm font-medium mb-2">
                        Título
                    </label>
                    <input type="text" th:field="*{title}" required
                           class="w-full bg-dark-700 border border-dark-600 rounded-lg px-4 py-2.5 text-gray-100 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors" />
                </div>
                
                <div class="mb-6">
                    <label class="block text-gray-300 text-sm font-medium mb-2">
                        Autor
                    </label>
                    <input type="text" th:field="*{author}" required
                           class="w-full bg-dark-700 border border-dark-600 rounded-lg px-4 py-2.5 text-gray-100 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors" />
                </div>
                
                <div class="mb-8">
                    <label class="block text-gray-300 text-sm font-medium mb-2">
                        Conteúdo
                    </label>
                    <textarea th:field="*{content}" rows="12" required
                              class="w-full bg-dark-700 border border-dark-600 rounded-lg px-4 py-2.5 text-gray-100 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors"></textarea>
                </div>
                
                <div class="flex items-center justify-between">
                    <button type="submit" 
                            class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                        Salvar
                    </button>
                    <a href="/admin" 
                       class="px-6 py-3 text-gray-400 hover:text-gray-300 transition-colors">
                        Cancelar
                    </a>
                </div>
            </form>
        </div>
    </div>
</div>
```

### 6.3 Características do Design

O tema escuro implementado possui as seguintes características:

1. **Cores:**
   - Fundo principal: bg-dark-900 (azul escuro profundo)
   - Elementos secundários: bg-dark-800 (um pouco mais claro)
   - Bordas: border-dark-700 (contraste sutil)
   - Texto: variações de gray-100 a gray-400
   - Destaques: gradientes de blue-500 a purple-600

2. **Tipografia:**
   - Fonte Inter para melhor legibilidade
   - Tamanhos de texto hierárquicos
   - Gradientes nos títulos principais

3. **Interações:**
   - Transições suaves em hover states
   - Feedback visual em inputs e botões
   - Efeitos de hover em cards e tabelas

4. **Componentes:**
   - Cards com bordas sutis
   - Botões com estados hover
   - Inputs com foco estilizado
   - Tabelas com linhas alternadas

5. **Responsividade:**
   - Layout adaptativo
   - Espaçamento consistente
   - Containers com largura máxima

Esta estilização cria uma interface moderna e agradável aos olhos, mantendo a usabilidade e acessibilidade.

## Estrutura Final do Projeto
A estrutura final do projeto ficará organizada da seguinte forma:

```
src/main/kotlin/com/blog/BlogApplication.kt
src/main/kotlin/com/blog/config/SecurityConfig.kt
src/main/kotlin/com/blog/controller/AdminController.kt
src/main/kotlin/com/blog/controller/AuthController.kt
src/main/kotlin/com/blog/controller/BlogController.kt
src/main/kotlin/com/blog/model/Post.kt
src/main/kotlin/com/blog/model/User.kt
src/main/kotlin/com/blog/repository/PostRepository.kt
src/main/kotlin/com/blog/repository/UserRepository.kt
src/main/kotlin/com/blog/service/UserService.kt
```