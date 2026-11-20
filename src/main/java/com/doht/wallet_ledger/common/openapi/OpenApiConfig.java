package com.doht.wallet_ledger.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình OpenAPI 3 / Swagger UI cho hệ thống Wallet Ledger,
 * bao gồm khai báo cơ chế xác thực Bearer JWT.
 */
@Configuration
@EnableConfigurationProperties(SwaggerUiProperties.class)
public class OpenApiConfig {

    /** Tên security scheme dùng chung cho các endpoint yêu cầu JWT. */
    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI walletLedgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wallet Ledger API")
                        .version("v1")
                        .description("API backend quản lý ví tài chính và sổ cái kế toán kép (double-entry ledger)."))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_AUTH_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT access token nhận được từ API đăng nhập hoặc đăng ký.")
                        ));
    }
}
