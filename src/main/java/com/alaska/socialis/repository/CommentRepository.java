package com.alaska.socialis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    public List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    Optional<Comment> findByUid(String uid);

    public List<Comment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
