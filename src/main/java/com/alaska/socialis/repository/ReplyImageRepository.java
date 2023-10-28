package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaska.socialis.model.ReplyImage;

public interface ReplyImageRepository extends JpaRepository<ReplyImage, Long> {
    List<ReplyImage> findAllByReplyId(Long replyId);
}
