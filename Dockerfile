# 246.3 MB，Javet 的 native 库（libjavet.so）是为 glibc 构建的动态链接库而 eclipse-temurin:21-jre-alpine 基于 Alpine Linux，使用 musl libc（一种轻量级替代品）。Alpine 缺少 glibc 的动态链接器 ld-linux-x86-64.so.2，导致加载失败
# FROM eclipse-temurin:21-jre-alpine

# 构建阶段。基于 Ubuntu，326.5 MB
FROM eclipse-temurin:21-jre-jammy

# 声明目标架构参数，供当前构建阶段使用
ARG TARGETARCH

WORKDIR /sonovel

# 根据目标架构复制对应的编译产物
COPY docker-context/${TARGETARCH}/app.jar ./app.jar
COPY docker-context/${TARGETARCH}/config.ini ./config.ini
COPY docker-context/${TARGETARCH}/rules/ ./rules/

EXPOSE 7765

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=GMT+08 \
  -jar app.jar"]