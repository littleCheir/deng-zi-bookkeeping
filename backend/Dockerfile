# syntax=docker/dockerfile:1
# 后端构建阶段：使用 Maven + JDK 21 构建可执行 JAR
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# 先复制 pom 再复制源码，源码变化时仍可复用依赖下载层
COPY pom.xml ./
COPY src ./src

RUN mvn -B -q -DskipTests package

# 运行阶段：只带 JRE，减小镜像体积
FROM eclipse-temurin:21-jre

ENV TZ=Asia/Shanghai
WORKDIR /app

# 以非 root 用户运行，降低容器被利用后的影响
RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app app \
    && chown -R app:app /app

COPY --from=build /workspace/target/expense-tracker-*.jar /app/app.jar
RUN chown app:app /app/app.jar

USER app
EXPOSE 8080

# MaxRAMPercentage 让 JVM 按容器内存上限自适应堆大小
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
