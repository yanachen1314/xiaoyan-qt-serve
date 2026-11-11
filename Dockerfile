# 使用 Eclipse Temurin（IBM 提供的 OpenJDK 发行版，更稳定）
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /home/yan/project/xiaoyanqt-serve

# 将本地的 JAR 文件复制到容器中
COPY xiaoyanqt-serve-*.jar app.jar

# 暴露应用运行的端口
EXPOSE 8080

# 定义容器启动时运行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]