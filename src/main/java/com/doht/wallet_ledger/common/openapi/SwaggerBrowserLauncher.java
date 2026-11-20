package com.doht.wallet_ledger.common.openapi;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Tự động mở trình duyệt mặc định đến trang Swagger UI sau khi web server khởi động xong.
 * Không chạy trong môi trường test (MockMvc) hoặc khi {@code app.swagger.auto-open=false}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.swagger", name = "auto-open", havingValue = "true")
@ConditionalOnProperty(prefix = "springdoc.swagger-ui", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerBrowserLauncher {

    private final Environment environment;
    private final AtomicInteger serverPort = new AtomicInteger(-1);

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        serverPort.set(event.getWebServer().getPort());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        int port = serverPort.get();
        if (port <= 0) {
            return;
        }

        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String swaggerUrl = "http://localhost:" + port + contextPath + "/swagger-ui.html";
        log.info("Đang mở Swagger UI tại: {}", swaggerUrl);
        openBrowser(swaggerUrl);
    }

    private void openBrowser(String url) {
        if (GraphicsEnvironment.isHeadless()
                || !Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            log.info("Môi trường hiện tại không hỗ trợ tự động mở trình duyệt. Truy cập thủ công tại: {}", url);
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ex) {
            log.warn("Không thể tự động mở trình duyệt tới {}: {}", url, ex.getMessage());
        }
    }
}
