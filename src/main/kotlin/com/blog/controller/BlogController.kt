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