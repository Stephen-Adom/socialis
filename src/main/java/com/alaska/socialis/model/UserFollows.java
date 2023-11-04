package com.alaska.socialis.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "user_follows")
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date followedAt;
}
