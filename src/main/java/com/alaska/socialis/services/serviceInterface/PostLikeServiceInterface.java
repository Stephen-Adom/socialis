package com.alaska.socialis.services.serviceInterface;

import com.alaska.socialis.model.dto.PostDto;

public interface PostLikeServiceInterface {
    public PostDto togglePostLike(Long userId, Long postId);
}
