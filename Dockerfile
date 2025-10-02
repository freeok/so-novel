# 246.3 MB，Javet 的 native 库（libjavet.so）是为 glibc 构建的动态链接库，而 eclipse-temurin:21-jre-alpine 基于 Alpine Linux，使用 musl libc（一种轻量级替代品）。Alpine 缺少 glibc 的动态链接器 ld-linux-x86-64.so.2，导致加载失败
# FROM eclipse-temurin:21-jre-alpine

# 构建阶段。基于 Ubuntu，326.5 MB
FROM eclipse-temurin:21-jre-jammy

WORKDIR /sonovel

COPY app.jar config.ini ./
COPY rules/ rules/

EXPOSE 7765

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=GMT+08 \
  -jar app.jar"]