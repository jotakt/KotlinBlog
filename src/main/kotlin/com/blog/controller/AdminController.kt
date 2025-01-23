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
            // Novo post
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