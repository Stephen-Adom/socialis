package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaska.socialis.model.CommentImages;

public interface CommentImageRepository extends JpaRepository<CommentImages, Long> {
    List<CommentImages> findAllByCommentId(Long id);
}
