package com.alaska.socialis.model;

import java.util.Date;

import lombok.Getter;

@Getter
public abstract class Like {
    private Long id;
    private Date createdAt;
    private User user;
}
