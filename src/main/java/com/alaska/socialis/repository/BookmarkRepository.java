package com.alaska.socialis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Bookmark;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserId(Long userId);

    List<Bookmark> findAllByContentIdAndContentType(Long contentId, String contentType);

    Optional<Bookmark> findByContentIdAndContentType(Long contentId, String contentType);
}
