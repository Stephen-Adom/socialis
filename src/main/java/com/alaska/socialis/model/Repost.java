package com.alaska.socialis.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "UNIQUE_REPOST_ID", columnList = "uid"),
        @Index(name = "FK_POST_ID", columnList = "post_id"),
        @Index(name = "FK_USER_ID", columnList = "user_id"),
        @Index(name = "FK_ORIGINAL_POST_ID", columnList = "original_post_id"),
        @Index(name = "FK_USER_POST_ID", columnList = "user_id, post_id"),
        @Index(name = "FK_ORIGINAL_USER_POST_ID", columnList = "user_id, post_id, original_post_id"),
})
public class Repost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String uid;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "original_post_id", referencedColumnName = "id")
    private Repost originalRepost;

    private String content;

    private int numberOfLikes = 0;

    private int numberOfComments = 0;

    private int numberOfBookmarks = 0;

    private int numberOfRepost = 0;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}
