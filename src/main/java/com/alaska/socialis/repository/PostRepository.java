package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import com.alaska.socialis.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    public Optional<List<Post>> findByUserId(Long userid);
}
