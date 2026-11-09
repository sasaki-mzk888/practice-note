package com.example.practicenote.repository;

import com.example.practicenote.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
