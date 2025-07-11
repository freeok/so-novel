FROM openjdk:17-jdk-slim

# WSL 默认缺少 libatomic1，末尾操作目的：清理 apt 缓存文件，减小最终镜像的体积
RUN apt-get update && apt-get install -y libatomic1 && rm -rf /var/lib/apt/lists/*

WORKDIR /sonovel

# 若启动容器时挂载，则不需要前 2 个 COPY 操作
COPY config.ini /sonovel/
COPY rules /sonovel/rules
COPY fonts /sonovel/fonts
COPY app.jar /sonovel/

ENTRYPOINT [ \
"java", \
"-Dfile.encoding=UTF-8", \
"-Duser.timezone=GMT+08", \
"-jar", \
"app.jar" \
]