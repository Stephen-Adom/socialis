package com.alaska.socialis.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
        @Index(name = "FK_FOLLOWS_FOLLOWER_ID", columnList = "follower_id"),
        @Index(name = "FK_FOLLOWS_FOLLOWING_ID", columnList = "following_id"),
        @Index(name = "FK_FOLLOWS_FOLLOWING_FOLLOWER_ID", columnList = "following_id, follower_id"),
})
@IdClass(UserFollowsKey.class)
public class UserFollows {

    @Id
    @ManyToOne
    @JoinColumn(name = "follower_id", referencedColumnName = "id")
    private User follower;

    @Id
    @ManyToOne
    @JoinColumn(name = "following_id", referencedColumnName = "id")
    private User following;

    @Column(name = "followed_at")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date followedAt;
}
