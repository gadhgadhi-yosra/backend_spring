package com.elfaddoui.backend.user.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 40)
    private String phone;

    @Column(length = 1200)
    private String avatarUrl;

    @Column(length = 2000)
    private String address;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    // reset password token (realistic, DB-based)
    private String resetToken;
    private Instant resetTokenExpiresAt;
    @Column(name = "reset_otp_hash")
    private String resetOtpHash;
    @Column(name = "reset_otp_expires_at")
    private Instant resetOtpExpiresAt;
    @Column(name = "reset_otp_attempts")
    private Integer resetOtpAttempts;

    private boolean enabled = true;

    public User() {}

    public User(String fullName, String email, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Instant getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(Instant resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }
    public String getResetOtpHash() { return resetOtpHash; }
    public void setResetOtpHash(String resetOtpHash) { this.resetOtpHash = resetOtpHash; }
    public Instant getResetOtpExpiresAt() { return resetOtpExpiresAt; }
    public void setResetOtpExpiresAt(Instant resetOtpExpiresAt) { this.resetOtpExpiresAt = resetOtpExpiresAt; }
    public Integer getResetOtpAttempts() { return resetOtpAttempts; }
    public void setResetOtpAttempts(Integer resetOtpAttempts) { this.resetOtpAttempts = resetOtpAttempts; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
