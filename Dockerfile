FROM eclipse-temurin:17-jre-jammy

# 设置工作目录
WORKDIR /app

# Maven 在 target 目录生成可执行 JAR，构建上下文为项目根目录
COPY target/xiaoyanqt-serve-*.jar /app/app.jar

# 暴露应用运行的端口
EXPOSE 8080

# 定义容器启动时运行的命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
