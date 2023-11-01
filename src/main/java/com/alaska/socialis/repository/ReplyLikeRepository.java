package com.alaska.socialis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.ReplyLike;

@Repository
public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {
    Optional<ReplyLike> findByUserIdAndReplyId(Long userId, Long replyId);

    List<ReplyLike> findAllByUserId(Long userId);
}
