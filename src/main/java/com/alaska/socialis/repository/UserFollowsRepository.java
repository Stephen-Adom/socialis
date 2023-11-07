package com.alaska.socialis.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaska.socialis.model.UserFollows;

public interface UserFollowsRepository extends JpaRepository<UserFollows, Long> {
    Set<UserFollows> findAllByFollowerId(Long followerId);

    Set<UserFollows> findAllByFollowingId(Long followingId);

    Optional<UserFollows> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
