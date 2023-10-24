package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public UserDetails findByUsername(String username);

    public Boolean existsByEmail(String email);

    public Boolean existsByUsername(String username);

    public Boolean existsByPhonenumber(String phonenumber);

    public Optional<User> findByEmail(String email);
}
