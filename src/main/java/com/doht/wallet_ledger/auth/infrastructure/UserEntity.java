package com.doht.wallet_ledger.auth.infrastructure;

import com.doht.wallet_ledger.auth.domain.UserRole;
import com.doht.wallet_ledger.auth.domain.UserStatus;
import com.doht.wallet_ledger.common.persistence.BaseSoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserEntity extends BaseSoftDeletableEntity {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public static UserEntity register(String normalizedEmail, String passwordHash, String fullName) {
        UserEntity user = new UserEntity();
        user.email = normalizedEmail;
        user.passwordHash = passwordHash;
        user.fullName = fullName;
        user.role = UserRole.USER;
        user.status = UserStatus.ACTIVE;
        user.failedLoginAttempts = 0;
        return user;
    }

    public boolean isPermanentlyBlocked() {
        return status == UserStatus.LOCKED || status == UserStatus.SUSPENDED;
    }

    public boolean isTemporarilyLockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void recordFailedLogin(Instant now, int maxFailedAttempts, Duration lockDuration) {
        this.failedLoginAttempts += 1;
        if (this.failedLoginAttempts >= maxFailedAttempts) {
            this.lockedUntil = now.plus(lockDuration);
        }
    }

    public void recordSuccessfulLogin(Instant now) {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = now;
    }
}
