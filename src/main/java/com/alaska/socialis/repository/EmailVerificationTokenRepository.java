package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.EmailVerificationToken;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    public Optional<EmailVerificationToken> findByToken(String token);

    public Optional<EmailVerificationToken> findByUserId(Long userid);
}
