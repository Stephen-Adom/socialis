package com.alaska.socialis.services.serviceInterface;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;

public interface ReplyServiceInterface {
    public Map<String, Object> createReply(Long userId, Long commentId, String content, MultipartFile[] multipartFiles)
            throws EntityNotFoundException;
}
