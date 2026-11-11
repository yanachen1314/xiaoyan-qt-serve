FROM eclipse-temurin:17-jre-jammy

# 设置工作目录
WORKDIR /app

# 将本地的 JAR 文件复制到容器中
COPY xiaoyanqt-serve-*.jar app.jar

# 暴露应用运行的端口
EXPOSE 8080

# 定义容器启动时运行的命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]