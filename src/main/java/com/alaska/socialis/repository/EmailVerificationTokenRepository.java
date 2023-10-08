package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaska.socialis.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    public Optional<EmailVerificationToken> findByToken(String token);
}
