package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.UserStory;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

}
