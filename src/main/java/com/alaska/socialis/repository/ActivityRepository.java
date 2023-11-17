package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

}
