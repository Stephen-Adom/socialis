package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query(value = "SELECT * FROM activity WHERE user_id = ?1 ORDER BY created_at DESC", nativeQuery = true)
    List<Activity> findAllByUserId(Long userId);
}
