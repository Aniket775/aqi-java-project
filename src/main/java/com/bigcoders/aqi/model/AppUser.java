package com.bigcoders.aqi.model;

import java.time.LocalDateTime;

public class AppUser {
    private Integer userId;
    private String username;
    private String email;
    private String passwordHash;
    private String role; // Admin, Analyst, Operator, Viewer
    private LocalDateTime createdAt;

    public AppUser() {}

    public AppUser(Integer userId, String username, String email, String passwordHash, String role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : "Viewer";
        this.createdAt = createdAt;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
