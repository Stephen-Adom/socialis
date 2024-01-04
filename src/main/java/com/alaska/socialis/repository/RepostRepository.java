package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Repost;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {
    @Query(value = "select * from Repost r where r.user_id = ?1 AND r.post_id=?2 and r.content IS NULL", nativeQuery = true)
    Optional<Repost> findByUserIdAndPostIdWithNoContent(Long userId, Long postId);

    @Query(value = "select * from Repost r where r.user_id = ?1 AND r.post_id=?2 and r.original_post_id=?3 and r.content IS NULL", nativeQuery = true)
    Optional<Repost> findByUserIdAndPostIdAndOriginalPostIdWithNoContent(Long userId, Long postId,
            Long original_post_id);
}
