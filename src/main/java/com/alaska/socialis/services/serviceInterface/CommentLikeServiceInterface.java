package com.alaska.socialis.services.serviceInterface;

import com.alaska.socialis.model.dto.CommentDto;

public interface CommentLikeServiceInterface {
    public CommentDto toggleCommentLike(Long userId, Long commentId);
}
