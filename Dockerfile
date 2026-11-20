# syntax=docker/dockerfile:1

# ==============================================================================
# Giai đoạn 1: Build ứng dụng bằng JDK 25 & Maven Wrapper
# ==============================================================================
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Sao chép Maven Wrapper và pom.xml trước để tận dụng Docker layer cache
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

# Sao chép mã nguồn và đóng gói file JAR
COPY src src
RUN ./mvnw -B package -DskipTests

# ==============================================================================
# Giai đoạn 2: Runtime gọn nhẹ chỉ gồm JRE 25 + user không có quyền root
# ==============================================================================
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=builder /workspace/target/wallet-ledger-*.jar /app/app.jar
RUN chown appuser:appgroup /app/app.jar

USER appuser

# Render tự động truyền biến môi trường PORT; mặc định 8080 khi chạy local
ENV PORT=8080
ENV SWAGGER_AUTO_OPEN=false
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.awt.headless=true"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
