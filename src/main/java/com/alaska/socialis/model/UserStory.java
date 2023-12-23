package com.alaska.socialis.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UserStory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "media_caption", nullable = true)
    private String mediaCaption;

    @Column(name = "expired_at", nullable = false)
    private Date expiredAt;

    @Column(name = "uploaded_at", nullable = false)
    private Date uploadedAt;

    @OneToMany(mappedBy = "story")
    private List<WatchedStory> watchedBy = new ArrayList<>();
}
