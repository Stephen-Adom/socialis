package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.StoryMedia;

@Repository
public interface StoryMediaRepository extends JpaRepository<StoryMedia, Long> {
}
