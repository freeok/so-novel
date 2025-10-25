#!/bin/bash
set -e  # 出现错误立即退出

# =====================
# Windows 发布脚本 (x64)
# =====================

# === 基本信息 ===
JRE_FILENAME="jre-21.0.8+9-windows_x64.zip"
JRE_DIRNAME="jdk-21.0.8+9-jre"
DIST_FILENAME="sonovel-windows.tar.gz"
DIST_DIRNAME="SoNovel"
DOWNLOAD_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.8%2B9/OpenJDK21U-jre_x64_windows_hotspot_21.0.8_9.zip"

# === 路径定义 ===
PROJECT_PATH="$( cd "$(dirname "$0")"/.. && pwd )"
DIST_PATH="$PROJECT_PATH/dist"
TARGET_DIR="$PROJECT_PATH/target/$DIST_DIRNAME"
JRE_PATH="bundle/$JRE_FILENAME"

prepare_dist_dir() {
    mkdir -p "$DIST_PATH"
    if [ -f "$JRE_PATH" ]; then
        echo "JRE 已存在，无需下载。"
    else
        echo "JRE 不存在，开始下载..."
        curl --retry 3 -C - -L -o "$JRE_PATH" "$DOWNLOAD_URL"

        # 检查下载是否成功
        if [ $? -eq 0 ]; then
            echo "下载完成，JRE 保存在 $JRE_PATH"
        else
            echo "下载失败，请检查网络或 URL。"
            exit 1
        fi
    fi
}

run_maven() {
    mvn clean package -Pwindows-x64 '-Dmaven.test.skip=true' '-DjrePath=runtime'
}

copy_files() {
    cp "bundle/$JRE_FILENAME" "$TARGET_DIR"
    cp "target/app-jar-with-dependencies.jar" "$TARGET_DIR/app.jar"
    cp -r bundle/rules "$TARGET_DIR/"
    cp bundle/config.ini bundle/sonovel.l4j.ini bundle/readme.txt "$TARGET_DIR"
    cp "bundle/支持 & 赞助.png" "$TARGET_DIR"
}

extract_jre() {
    cd "$TARGET_DIR"
    unzip -q "$JRE_FILENAME"
    mv "$JRE_DIRNAME" runtime
    rm "$JRE_FILENAME"
}

package_artifacts() {
    cd "$PROJECT_PATH/target"
    tar czf "$DIST_FILENAME" "$DIST_DIRNAME"
    mv "$DIST_FILENAME" "$DIST_PATH"
}

main() {
    cd "$PROJECT_PATH"
    prepare_dist_dir
    run_maven
    copy_files
    extract_jre
    package_artifacts
    echo "✅ Windows x64 构建完成！产物: $DIST_FILENAME"
}

main