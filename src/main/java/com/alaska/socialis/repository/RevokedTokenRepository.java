package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.RevokedTokens;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedTokens, Long> {
    public Optional<RevokedTokens> findByToken(String token);
}
