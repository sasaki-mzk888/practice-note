package com.example.practicenote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "users")
public class AppUser {

    private static final ZoneId JAPAN_TIME_ZONE = ZoneId.of("Asia/Tokyo");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 100)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "user_name", nullable = false, length = 80)
    private String userName;

    @Column(name = "role", nullable = false, length = 16)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AppUser() { }

    public AppUser(String loginId, String passwordHash, String userName, AccountRole role) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.userName = userName;
        this.role = role.name();
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now(JAPAN_TIME_ZONE);
    }

    public Long getUserId() { return userId; }
    public String getLoginId() { return loginId; }
    public String getPasswordHash() { return passwordHash; }
    public String getUserName() { return userName; }
    public AccountRole getRole() { return AccountRole.valueOf(role); }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
