#!/bin/bash

# 设置权限
chmod 777 ./runtime/bin/java || { echo "设置权限失败"; exit 1; }

# 检查文件是否存在
[ -f ./runtime/bin/java ] || { echo "未找到 Java 可执行文件"; exit 1; }
[ -f app.jar ] || { echo "未找到 app.jar"; exit 1; }
[ -f config.ini ] || { echo "未找到 config.ini"; exit 1; }

# 运行 Java 应用
./runtime/bin/java \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -Dconfig.file=config.ini \
  -Dmode=tui \
  -jar app.jar || { echo "运行 Java 应用失败"; exit 1; }