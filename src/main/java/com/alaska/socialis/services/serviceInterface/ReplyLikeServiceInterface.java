package com.alaska.socialis.services.serviceInterface;

import com.alaska.socialis.model.dto.ReplyDto;

public interface ReplyLikeServiceInterface {
    public ReplyDto toggleReplyLike(Long userId, Long replyId);
}
