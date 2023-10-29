package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.CommentImages;

@Repository
public interface CommentImageRepository extends JpaRepository<CommentImages, Long> {
    List<CommentImages> findAllByCommentId(Long id);
}
