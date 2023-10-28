package com.alaska.socialis.services.serviceInterface;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.ReplyDto;

public interface ReplyServiceInterface {
    public Map<String, Object> createReply(Long userId, Long commentId, String content, MultipartFile[] multipartFiles)
            throws EntityNotFoundException;

    public List<ReplyDto> fetchAllReplies(Long commentId);

    public ReplyDto editReply(Long id, String content, MultipartFile[] multipartFile) throws EntityNotFoundException;

    public void deleteReply(Long id) throws EntityNotFoundException;
}
