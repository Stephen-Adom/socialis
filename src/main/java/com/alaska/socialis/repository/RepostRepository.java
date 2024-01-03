package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Repost;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {

}
