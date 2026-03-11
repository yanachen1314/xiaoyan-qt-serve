# 使用官方 Java 17 运行时作为基础镜像
FROM openjdk:17-jre

# 设置工作目录
WORKDIR /home/yan/project/xiaoyanqt-serve

# 将本地的 JAR 文件复制到容器中
COPY xiaoyanQt-serve-1.0.0.jar app.jar

# 暴露应用运行的端口
EXPOSE 8080

# 定义容器启动时运行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]