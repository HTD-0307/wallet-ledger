package com.doht.wallet_ledger.auth.api;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.LoginRequest;
import com.doht.wallet_ledger.auth.application.dto.LogoutRequest;
import com.doht.wallet_ledger.auth.application.dto.RefreshTokenRequest;
import com.doht.wallet_ledger.auth.application.dto.RegisterRequest;
import com.doht.wallet_ledger.auth.application.dto.UserProfileResponse;
import com.doht.wallet_ledger.auth.application.handler.GetCurrentUserHandler;
import com.doht.wallet_ledger.auth.application.handler.LoginHandler;
import com.doht.wallet_ledger.auth.application.handler.LogoutHandler;
import com.doht.wallet_ledger.auth.application.handler.RefreshTokenHandler;
import com.doht.wallet_ledger.auth.application.handler.RegisterHandler;
import com.doht.wallet_ledger.common.openapi.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, làm mới token và quản lý phiên người dùng")
public class AuthController {

    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final RefreshTokenHandler refreshTokenHandler;
    private final LogoutHandler logoutHandler;
    private final GetCurrentUserHandler getCurrentUserHandler;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerHandler.handle(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng email và mật khẩu")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return loginHandler.handle(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token bằng refresh token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenHandler.handle(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Đăng xuất và thu hồi refresh token")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    public void logout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody LogoutRequest request) {
        logoutHandler.handle(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin hồ sơ người dùng hiện tại")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    public UserProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        return getCurrentUserHandler.handle(UUID.fromString(jwt.getSubject()));
    }
}
