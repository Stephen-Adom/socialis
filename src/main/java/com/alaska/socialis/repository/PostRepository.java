package com.alaska.socialis.repository;

import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
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

    Window<Post> findFirst5ByScheduledAtIsNullOrderByCreatedAtDesc(OffsetScrollPosition position);

    public List<Post> findAllByScheduledAtNotNullAndScheduledAtBefore(LocalDateTime currentTime);
}
