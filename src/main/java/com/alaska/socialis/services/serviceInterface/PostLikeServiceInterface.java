package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.PostDto;

public interface PostLikeServiceInterface {
    public PostDto togglePostLike(Long userId, Long postId);

    public List<Object> fetchAllLikesByUser(Long userId) throws EntityNotFoundException;
}
