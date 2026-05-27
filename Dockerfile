# --- TẦNG 1: Biên dịch ứng dụng (Build Stage) ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Sao chép pom.xml để tải dependencies trước (tận dụng cache của Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sao chép mã nguồn và biên dịch thành tệp .jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- TẦNG 2: Chạy ứng dụng (Runtime Stage) ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Sao chép file .jar đã đóng gói từ Tầng 1 sang
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080 của Spring Boot backend
EXPOSE 8080

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
