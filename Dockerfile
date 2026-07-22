# ===================================================================
# open-admin 多阶段 Docker 构建
# ===================================================================
# 后端构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build-backend
WORKDIR /build

COPY pom.xml ./
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn package -DskipTests -B -q

# ===================================================================
# 前端构建阶段
FROM node:24 AS build-frontend
WORKDIR /build

COPY web/package.json web/ ./
RUN npm install

COPY web/ ./
RUN npm run build

# ===================================================================
# 运行阶段
FROM eclipse-temurin:21-jre
WORKDIR /app

# 从后端构建阶段复制 JAR
COPY --from=build-backend /build/target/*.jar app.jar

# 从前端构建阶段复制静态文件到 static/ 目录
COPY --from=build-frontend /build/dist/ ./static/

# 时区
ENV TZ=Asia/Shanghai

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]