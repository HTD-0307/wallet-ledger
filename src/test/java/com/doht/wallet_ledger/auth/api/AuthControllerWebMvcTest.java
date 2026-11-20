package com.doht.wallet_ledger.auth.api;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.UserProfileResponse;
import com.doht.wallet_ledger.auth.application.handler.GetCurrentUserHandler;
import com.doht.wallet_ledger.auth.application.handler.LoginHandler;
import com.doht.wallet_ledger.auth.application.handler.LogoutHandler;
import com.doht.wallet_ledger.auth.application.handler.RefreshTokenHandler;
import com.doht.wallet_ledger.auth.application.handler.RegisterHandler;
import com.doht.wallet_ledger.auth.domain.UserRole;
import com.doht.wallet_ledger.auth.domain.UserStatus;
import com.doht.wallet_ledger.auth.infrastructure.SecurityConfig;
import com.doht.wallet_ledger.common.exception.GlobalExceptionHandler;
import com.doht.wallet_ledger.common.i18n.MessageKeys;
import com.doht.wallet_ledger.common.i18n.MessageService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, MessageService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-with-at-least-32-bytes-123456",
        "app.jwt.issuer=wallet-ledger"
})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterHandler registerHandler;
    @MockitoBean
    private LoginHandler loginHandler;
    @MockitoBean
    private RefreshTokenHandler refreshTokenHandler;
    @MockitoBean
    private LogoutHandler logoutHandler;
    @MockitoBean
    private GetCurrentUserHandler getCurrentUserHandler;

    @Test
    void shouldReturn400ProblemDetailWithMessageKeyWhenRegisterPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"weak","fullName":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.messageKey").value(MessageKeys.COMMON_ERROR_VALIDATION_FAILED_DETAIL))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    void shouldResolveVietnameseMessagesWhenAcceptLanguageIsVi() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "vi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"weak","fullName":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dữ liệu không hợp lệ"))
                .andExpect(jsonPath("$.errors.email[0]").value("Email không đúng định dạng."));
    }

    @Test
    void shouldReturn201WhenRegisterPayloadIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "alice@example.com", "Alice Nguyen", UserRole.USER, UserStatus.ACTIVE, Instant.now()
        );
        when(registerHandler.handle(any())).thenReturn(AuthResponse.bearer("access-jwt", "refresh-tok", 900L, profile));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"Password123","fullName":"Alice Nguyen"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("alice@example.com"));
    }

    @Test
    void shouldReturn401ProblemJsonWithMessageKeyWhenAccessingMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.messageKey").value(MessageKeys.COMMON_ERROR_UNAUTHORIZED_DETAIL));
    }

    @Test
    void shouldReturn200WhenAccessingMeWithValidJwt() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "alice@example.com", "Alice Nguyen", UserRole.USER, UserStatus.ACTIVE, Instant.now()
        );
        when(getCurrentUserHandler.handle(userId)).thenReturn(profile);

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt().jwt(j -> j.subject(userId.toString()).claim("role", "USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldPermitSwaggerAndOpenApiEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is(not(401)));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is(not(401)));
    }
}
