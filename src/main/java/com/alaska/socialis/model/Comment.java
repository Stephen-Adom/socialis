package com.alaska.socialis.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "comments", uniqueConstraints = @UniqueConstraint(name = "comment_uid_unique", columnNames = "uid"), indexes = {
        @Index(name = "UNIQUE_COMMENT_LABEL", columnList = "uid"),
        @Index(name = "FK_COMMENT_USER_ID", columnList = "user_id"),
        @Index(name = "FK_COMMENT_POST_ID", columnList = "post_id"),
        @Index(name = "FK_COMMENT_USER_POST_ID", columnList = "user_id, post_id"),
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uid", unique = true, nullable = false)
    private String uid;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @Column(columnDefinition = "VARCHAR(255)")
    private String content;

    @Builder.Default
    private int numberOfLikes = 0;

    @Builder.Default
    private int numberOfReplies = 0;

    @Builder.Default
    private int numberOfBookmarks = 0;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    List<CommentImages> commentImages = new ArrayList<CommentImages>();

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    List<Reply> replies = new ArrayList<Reply>();

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    List<CommentLike> likes = new ArrayList<CommentLike>();
}
