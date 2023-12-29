package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.WatchedStory;

@Repository
public interface WatchedStoryRepository extends JpaRepository<WatchedStory, Long> {
    public Optional<WatchedStory> findByUserIdAndMediaId(Long userId, Long mediaId);
}
