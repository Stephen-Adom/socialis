package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    public List<Reply> findByCommentIdOrderByCreatedAtDesc(Long commentId);

    public List<Reply> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
