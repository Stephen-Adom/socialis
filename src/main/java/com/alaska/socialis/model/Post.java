package com.alaska.socialis.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts", uniqueConstraints = @UniqueConstraint(name = "post_uid_unique", columnNames = "uid"), indexes = {
        @Index(name = "UNIQUE_POST_LABEL", columnList = "uid"),
        @Index(name = "FK_POST_USER_ID", columnList = "user_id"),
        @Index(name = "FK_ORIGINAL_POST_ID", columnList = "original_post_id"),
        @Index(name = "FK_USER_ORIGINAL_POST_ID", columnList = "user_id, original_post_id"),
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uid", nullable = false, unique = true)
    private String uid;

    private String content;

    @Builder.Default
    private int numberOfLikes = 0;

    @Builder.Default
    private int numberOfComments = 0;

    @Builder.Default
    private int numberOfBookmarks = 0;

    @Builder.Default
    private int numberOfRepost = 0;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<PostImage> postImages = new ArrayList<PostImage>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    List<Comment> comments = new ArrayList<Comment>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    List<PostLike> likes = new ArrayList<PostLike>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Repost> reposts = new ArrayList<Repost>();

    @Column(name = "scheduled_at", nullable = true)
    private LocalDateTime scheduledAt;

    @ManyToOne
    @JoinColumn(name = "original_post_id", referencedColumnName = "id")
    private Post originalPost;
}
