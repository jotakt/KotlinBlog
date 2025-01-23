package com.blog.controller

import com.blog.model.Post
import com.blog.repository.PostRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/cms")
class CMSController(private val postRepository: PostRepository) {

    @GetMapping
    fun dashboard(model: Model): String {
        model.addAttribute("posts", postRepository.findAll())
        return "cms/dashboard"
    }

    @GetMapping("/post/new")
    fun newPost(model: Model): String {
        model.addAttribute("post", Post(title = "", content = "", author = ""))
        return "cms/edit"
    }

    @PostMapping("/post/save")
    fun savePost(@ModelAttribute post: Post): String {
        postRepository.save(post)
        return "redirect:/cms"
    }

    @GetMapping("/post/edit/{id}")
    fun editPost(@PathVariable id: Long, model: Model): String {
        val post = postRepository.findById(id).orElseThrow()
        model.addAttribute("post", post)
        return "cms/edit"
    }

    @PostMapping("/post/delete/{id}")
    fun deletePost(@PathVariable id: Long): String {
        postRepository.deleteById(id)
        return "redirect:/cms"
    }
} 