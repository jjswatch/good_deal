# 使用官方 Java 17
FROM eclipse-temurin:17-jdk

# 設定工作目錄
WORKDIR /app

# 複製 pom.xml 先下載依賴（加速）
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# 複製專案原始碼
COPY src ./src

# 打包 Spring Boot
RUN ./mvnw clean package -DskipTests

# 啟動 Spring Boot
CMD ["java", "-jar", "target/*.jar"]
