package com.alaska.socialis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.services.PostLikeService;

@RestController
@RequestMapping("/api")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    @GetMapping("/{userId}/{postId}/like")
    public PostDto togglePostLike(@PathVariable("userId") Long userId, @PathVariable("postId") Long postId) {
        PostDto postDto = this.postLikeService.togglePostLike(userId, postId);

        return postDto;
    }
}
