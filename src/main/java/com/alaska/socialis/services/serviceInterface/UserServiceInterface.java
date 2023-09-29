package com.alaska.socialis.services.serviceInterface;

import org.springframework.security.core.userdetails.UserDetails;

import com.alaska.socialis.exceptions.UserNotFoundException;

public interface UserServiceInterface {
    public UserDetails fetchUserDetailsByUsername(String username) throws UserNotFoundException;
}
