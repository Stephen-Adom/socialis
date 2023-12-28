package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Story;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    public Optional<Story> findByUserId(Long userId);
}
