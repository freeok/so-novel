#!/bin/bash
set -e

# ==========================
# Linux 发布脚本 (x64, arm64)
# 用法：
#   ./release-linux.sh [ARCH]
# 示例：
#   ./release-linux.sh x64
#   ./release-linux.sh arm64
# 默认：x64
# ==========================

ARCH="${1:-x64}"
JRE_FILENAME="jre-21.0.8+9-linux_${ARCH}.tar.gz"
JRE_DIRNAME="jdk-21.0.8+9-jre"
JRE_PATH="bundle/$JRE_FILENAME"
# 输出文件名和目录名根据架构区分
DIST_FILENAME="sonovel-linux_${ARCH}.tar.gz"
DIST_DIRNAME="sonovel-linux_${ARCH}"
PROJECT_PATH="$( cd "$(dirname "$0")"/.. && pwd )"

echo "🏗️ 开始构建 Linux [$ARCH]..."

arch_alias=""
if [ "$ARCH" = "x64" ]; then
  arch_alias="x64"
elif [ "$ARCH" = "arm64" ]; then
  arch_alias="aarch64"
else
    echo "❌ 不支持的架构: $ARCH，可选值：x64|arm64"
    exit 1
fi
DOWNLOAD_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.8%2B9/OpenJDK21U-jre_${arch_alias}_linux_hotspot_21.0.8_9.tar.gz"

cd "$PROJECT_PATH" || exit

# 检查 JRE 文件是否存在
if [ -f "$JRE_PATH" ]; then
    echo "$JRE_FILENAME 已存在，无需下载。"
else
    echo "$JRE_FILENAME 不存在，开始下载..."
    curl --retry 3 -C - -L -o "$JRE_PATH" "$DOWNLOAD_URL"
    # 检查下载是否成功
    if [ $? -eq 0 ]; then
        echo "下载完成，JRE 保存在 $JRE_PATH"
    else
        echo "下载失败，请检查网络或 URL。"
        exit 1
    fi
fi

# Maven 打包
mvn clean package -P"linux-${ARCH}" -Dmaven.test.skip=true -DjrePath=runtime

# 创建产物目录
mkdir -p dist
mkdir -p "target/$DIST_DIRNAME"

# 复制文件
cp "bundle/$JRE_FILENAME" "target/$DIST_DIRNAME"
cp -r bundle/rules "target/$DIST_DIRNAME"
cp bundle/config.ini bundle/readme.txt bundle/run-linux.sh CHANGELOG_ALL.md "target/$DIST_DIRNAME"

# 移动 jar 包
cd target
mv app-jar-with-dependencies.jar app.jar
cp app.jar "$DIST_DIRNAME"

# 解压 JRE
cd "$DIST_DIRNAME"
tar zxf "$JRE_FILENAME" && rm "$JRE_FILENAME"
mv "$JRE_DIRNAME" runtime
cd ..

# 打包压缩
tar czf "$DIST_FILENAME" "$DIST_DIRNAME"
mv "$DIST_FILENAME" "$PROJECT_PATH/dist"

echo "✅ Linux ${ARCH} 构建完成！产物: $DIST_FILENAME"