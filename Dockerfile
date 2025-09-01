FROM eclipse-temurin:17-jre-jammy

# Javet 4.1.6 已修复 libatomic Linux 的链接问题，升级后可删除此行
RUN apt-get update && apt-get install -y libatomic1 && rm -rf /var/lib/apt/lists/*

WORKDIR /sonovel

COPY app.jar /sonovel/

ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-Duser.timezone=GMT+08","-jar","app.jar"]