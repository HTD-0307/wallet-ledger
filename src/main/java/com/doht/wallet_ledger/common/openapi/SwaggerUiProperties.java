package com.doht.wallet_ledger.common.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Thuộc tính cấu hình tiện ích cho Swagger UI.
 *
 * @param autoOpen tự động mở trình duyệt tới trang Swagger UI khi ứng dụng khởi động xong
 */
@ConfigurationProperties(prefix = "app.swagger")
public record SwaggerUiProperties(boolean autoOpen) {
}
