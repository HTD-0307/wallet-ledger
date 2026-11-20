package com.doht.wallet_ledger.auth.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.LoginRequest;
import com.doht.wallet_ledger.auth.application.dto.LogoutRequest;
import com.doht.wallet_ledger.auth.application.dto.RefreshTokenRequest;
import com.doht.wallet_ledger.auth.application.dto.RegisterRequest;
import com.doht.wallet_ledger.auth.domain.UserRole;
import com.doht.wallet_ledger.auth.domain.exception.AccountLockedException;
import com.doht.wallet_ledger.auth.domain.exception.EmailAlreadyExistsException;
import com.doht.wallet_ledger.auth.domain.exception.InvalidCredentialsException;
import com.doht.wallet_ledger.auth.domain.exception.InvalidRefreshTokenException;
import com.doht.wallet_ledger.auth.infrastructure.JwtTokenService;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenEntity;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenRepository;
import com.doht.wallet_ledger.auth.infrastructure.UserEntity;
import com.doht.wallet_ledger.auth.infrastructure.UserRepository;
import com.doht.wallet_ledger.common.persistence.BaseEntity;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthHandlersTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-20T02:00:00Z");

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;

    private RegisterHandler registerHandler;
    private LoginHandler loginHandler;
    private RefreshTokenHandler refreshTokenHandler;
    private LogoutHandler logoutHandler;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        AuthSessionIssuer sessionIssuer = new AuthSessionIssuer(jwtTokenService, refreshTokenRepository);
        registerHandler = new RegisterHandler(userRepository, passwordEncoder, sessionIssuer, fixedClock);
        loginHandler = new LoginHandler(userRepository, passwordEncoder, sessionIssuer, fixedClock);
        refreshTokenHandler = new RefreshTokenHandler(userRepository, refreshTokenRepository, jwtTokenService, fixedClock);
        logoutHandler = new LogoutHandler(refreshTokenRepository, jwtTokenService, fixedClock);
    }

    @Test
    void shouldRegisterNewUserAndIssueTokens() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByEmailIgnoreCaseAndDeletedFalse("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("$2a$12$hashed");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity saved = inv.getArgument(0);
            setId(saved, userId);
            return saved;
        });
        when(jwtTokenService.generateAccessToken(userId, "alice@example.com", UserRole.USER)).thenReturn("access-jwt");
        when(jwtTokenService.generateRawRefreshToken()).thenReturn("raw-refresh-1");
        when(jwtTokenService.hashRefreshToken("raw-refresh-1")).thenReturn("hash-refresh-1");
        when(jwtTokenService.computeRefreshTokenExpiry(FIXED_NOW)).thenReturn(FIXED_NOW.plusSeconds(604800));
        when(jwtTokenService.accessTokenExpiresInSeconds()).thenReturn(900L);

        AuthResponse response = registerHandler.handle(new RegisterRequest("  ALICE@Example.com ", "Secret123", "  Alice Nguyen "));

        assertThat(response.accessToken()).isEqualTo("access-jwt");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-1");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.user().id()).isEqualTo(userId);
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        assertThat(response.user().fullName()).isEqualTo("Alice Nguyen");
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldRejectDuplicateEmailOnRegister() {
        when(userRepository.existsByEmailIgnoreCaseAndDeletedFalse("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerHandler.handle(new RegisterRequest("alice@example.com", "Secret123", "Alice")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldLockAccountAfterMaxFailedLoginAttempts() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.register("bob@example.com", "$2a$12$hashed", "Bob");
        setId(user, userId);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("bob@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1", "$2a$12$hashed")).thenReturn(false);

        for (int i = 0; i < LoginHandler.MAX_FAILED_LOGIN_ATTEMPTS; i++) {
            assertThatThrownBy(() -> loginHandler.handle(new LoginRequest("bob@example.com", "WrongPass1")))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        assertThat(user.getFailedLoginAttempts()).isEqualTo(LoginHandler.MAX_FAILED_LOGIN_ATTEMPTS);
        assertThat(user.getLockedUntil()).isEqualTo(FIXED_NOW.plus(LoginHandler.LOCK_DURATION));

        assertThatThrownBy(() -> loginHandler.handle(new LoginRequest("bob@example.com", "WrongPass1")))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void shouldRotateRefreshTokenOnValidRefresh() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.register("alice@example.com", "$2a$12$hashed", "Alice");
        setId(user, userId);

        RefreshTokenEntity existing = RefreshTokenEntity.issue(userId, "old-hash", FIXED_NOW.plusSeconds(3600));
        when(jwtTokenService.hashRefreshToken("old-raw")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHash("old-hash")).thenReturn(Optional.of(existing));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(jwtTokenService.generateRawRefreshToken()).thenReturn("new-raw");
        when(jwtTokenService.hashRefreshToken("new-raw")).thenReturn("new-hash");
        when(jwtTokenService.computeRefreshTokenExpiry(FIXED_NOW)).thenReturn(FIXED_NOW.plusSeconds(604800));
        when(jwtTokenService.generateAccessToken(userId, "alice@example.com", UserRole.USER)).thenReturn("new-access");
        when(jwtTokenService.accessTokenExpiresInSeconds()).thenReturn(900L);

        AuthResponse response = refreshTokenHandler.handle(new RefreshTokenRequest("old-raw"));

        assertThat(existing.isRevoked()).isTrue();
        assertThat(existing.getReplacedByTokenHash()).isEqualTo("new-hash");
        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-raw");
    }

    @Test
    void shouldRevokeAllActiveSessionsWhenRevokedRefreshTokenIsReused() {
        UUID userId = UUID.randomUUID();
        RefreshTokenEntity revokedToken = RefreshTokenEntity.issue(userId, "stolen-hash", FIXED_NOW.plusSeconds(3600));
        revokedToken.revoke(FIXED_NOW.minusSeconds(60), "newer-hash");

        when(jwtTokenService.hashRefreshToken("stolen-raw")).thenReturn("stolen-hash");
        when(refreshTokenRepository.findByTokenHash("stolen-hash")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenHandler.handle(new RefreshTokenRequest("stolen-raw")))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(userId), eq(FIXED_NOW));
    }

    @Test
    void shouldNotRevokeAnotherUsersTokenOnLogout() {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        RefreshTokenEntity token = RefreshTokenEntity.issue(ownerId, "owner-hash", FIXED_NOW.plusSeconds(3600));

        when(jwtTokenService.hashRefreshToken("owner-raw")).thenReturn("owner-hash");
        when(refreshTokenRepository.findByTokenHash("owner-hash")).thenReturn(Optional.of(token));

        logoutHandler.handle(attackerId, new LogoutRequest("owner-raw"));

        assertThat(token.isRevoked()).isFalse();
        verify(refreshTokenRepository, never()).save(any());
    }

    private static void setId(BaseEntity entity, UUID id) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
