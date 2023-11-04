package com.alaska.socialis.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserFollowsKey implements Serializable {

    Long follower;
    Long following;
}
