package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaska.socialis.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

}
