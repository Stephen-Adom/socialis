package com.alaska.socialis.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.StoryMedia;

@Repository
public interface StoryMediaRepository extends JpaRepository<StoryMedia, Long> {
    Page<StoryMedia> findAllByExpiredAtLessThanEqual(Date currentDate, PageRequest of);
}
