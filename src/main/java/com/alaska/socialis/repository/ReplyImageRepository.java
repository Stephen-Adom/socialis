package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.ReplyImage;

@Repository
public interface ReplyImageRepository extends JpaRepository<ReplyImage, Long> {
    List<ReplyImage> findAllByReplyId(Long replyId);
}
