package com.alaska.socialis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.ResetPasswordModel;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordModel, Long> {
    public Optional<ResetPasswordModel> findByToken(String token);

    public Optional<ResetPasswordModel> findByUserId(Long userId);

}
