package com.alaska.socialis.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(indexes = @Index(name = "FK_MEDIA_STORY_ID", columnList = "story_id"))
public class StoryMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "story_id", referencedColumnName = "id")
    private Story story;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "media_caption", nullable = true)
    private String mediaCaption;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "expired_at", nullable = false)
    private Date expiredAt;

    @Column(name = "uploaded_at", nullable = false)
    private Date uploadedAt;

    @OneToMany(mappedBy = "media", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<WatchedStory> watchedBy = new ArrayList<>();

}
