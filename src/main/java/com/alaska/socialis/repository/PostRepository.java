package com.alaska.socialis.repository;

import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.alaska.socialis.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    public Optional<List<Post>> findByUserId(Long userid);

    public int countByUserId(Long userid);

    public Optional<Post> findByIdAndUserId(Long id, Long userId);

    public List<Post> findAllByOrderByCreatedAtDesc();

    public Optional<Post> findByUid(String uid);

    public List<Post> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findFirst5ByScheduledAtIsNullOrderByCreatedAtDesc(OffsetScrollPosition position);

    @Query(value = "SELECT P.id AS post_id, R.id AS repost_id, P.content AS post_content, R.content AS repost_content, P.created_at AS post_created_at, R.created_at AS repost_created_at, P.number_of_bookmarks AS post_num_bookmarks, R.number_of_bookmarks AS repost_num_bookmarks, P.number_of_comments AS post_num_comments, R.number_of_comments AS repost_num_comments, P.number_of_likes AS post_num_likes, R.number_of_likes AS repost_num_likes, P.number_of_repost AS post_num_repost, P.scheduled_at, P.uid AS post_uid, R.post_id, P.user_id AS author_id, R.user_id AS repost_user_id  FROM Post P LEFT JOIN Repost R ON P.id = R.post_id WHERE P.scheduled_at IS NULL LIMIT ?1", nativeQuery = true)
    List<Post> findFirst5ByScheduledAtIsNullOrderByCreatedAtDescWithRepost(OffsetScrollPosition position);

    public List<Post> findAllByScheduledAtNotNullAndScheduledAtBefore(LocalDateTime currentTime);

    @Query(value = "SELECT * FROM posts p where p.user_id = ?1 AND p.original_post_id=?2 and p.content IS NULL", nativeQuery = true)
    Optional<Post> findByUserIdAndOriginalPostIdWithNoContent(Long userId, Long postId);

    @Query(value = "SELECT * FROM posts p where p.user_id = ?1 AND p.original_post_id=?2 and p.content IS NOT NULL", nativeQuery = true)
    Optional<Post> findByUserIdAndOriginalPostIdWithContent(Long userId, Long postId);

    List<Post> findAllByOriginalPostId(Long postId);
}
