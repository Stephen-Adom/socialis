package com.alaska.socialis.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "FK_POSTLIKE_POST_ID", columnList = "post_id"),
        @Index(name = "FK_POSTLIKE_USER_ID", columnList = "user_id"),
        @Index(name = "FK_POSTLIKE_TYPE_ID", columnList = "like_type"),
        @Index(name = "FK_POSTLIKE_POST_USER_ID", columnList = "user_id, post_id"),
        @Index(name = "FK_POSTLIKE_POST_USER_TYPE_ID", columnList = "user_id, post_id, like_type"),
})
public class PostLike extends Like {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String likeType;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date CreatedAt;
}
