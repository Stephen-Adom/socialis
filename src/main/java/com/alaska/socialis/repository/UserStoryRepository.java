package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import com.alaska.socialis.model.UserStory;

@Repository
public interface UserStoryRepository extends JpaRepository<Object, Long> {
    // public List<UserStory> findAllByUserIdOrderByUploadedAtDesc(Long userId);
}
