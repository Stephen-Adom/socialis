package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.WatchedStory;

public interface WatchedStoryRepository {
    public List<WatchedStory> findAllByStoryIdOrderByWatchedAtDesc(Long storyId);
}
